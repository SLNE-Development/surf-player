package dev.slne.surf.player.core.client.tab.service

import dev.slne.surf.api.core.event.*
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.player.api.tab.placeholder.TabPlaceholder
import dev.slne.surf.player.api.tab.placeholder.UpdateCondition
import dev.slne.surf.player.core.client.tab.config.tablistConfig
import dev.slne.surf.player.core.client.tab.config.TablistConfigFile
import dev.slne.surf.player.core.client.tab.entry.TabEntries
import dev.slne.surf.player.core.client.tab.platform.TabPlatform
import dev.slne.surf.player.core.client.tab.platform.TabPlayer
import dev.slne.surf.player.core.client.tab.redis.TabEntryUpdateRedisEvent
import dev.slne.surf.player.core.client.SurfPlayerClient.redisApi
import dev.slne.surf.player.core.client.tab.util.AdventureTablistRenderer
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.CancellationException
import net.kyori.adventure.text.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val log = logger()

/**
 * Coordinates tab list rendering, placeholder updates, player entries, and lifecycle management.
 *
 * Header and footer content is rendered from the configured templates and applied only when the
 * rendered result changes. Templates may reference built-in [BuiltinPlaceholder]s as well as
 * placeholders registered by other plugins. Known placeholders are invalidated according to their
 * [UpdateCondition], allowing templates to be re-rendered only when relevant data may have changed.
 *
 * Player entries are managed separately by [TabEntries].
 */
object TablistService {

    private val builtins = BuiltinPlaceholder.entries.associateBy { it.tagName }

    private val registered = ConcurrentHashMap<String, TabPlaceholder>()

    /**
     * Stores the event bus listener registrations installed for each placeholder using
     * [UpdateCondition.OnEvent].
     */
    private val eventTriggers = ConcurrentHashMap<String, List<Any>>()

    /**
     * Tracks changes to the set of available placeholders.
     *
     * Template analyses capture this version so cached analyses are discarded whenever a placeholder
     * is registered or unregistered.
     */
    private val placeholderVersion = AtomicInteger()

    @Volatile
    private var started = false

    /**
     * Generates monotonically increasing identifiers for captured [TablistValues] snapshots.
     *
     * This allows snapshots created concurrently to be ordered independently of the thread on which
     * they were captured.
     */
    private val generations = AtomicLong()

    @Volatile
    private var analyzed: AnalyzedTemplates? = null

    private val entries = TabEntries()

    private val additions = TablistAdditions(
        templates = { templates() },
        captureValues = { captureValues(templates()) },
        onlinePlayers = { TabPlatform.onlinePlayers() },
        onlinePlayerCount = { TabPlatform.onlinePlayerCount() },
        renderer = AdventureTablistRenderer,
        runUpdates = { block -> TabPlatform.launch { block() } }
    )

    /**
     * Starts the tab list service on the running [TabPlatform].
     *
     * The current templates are analyzed before the service is marked as started so invalid
     * configuration fails immediately during startup. Once initialized, the scheduler is started and
     * both header/footer content and all currently online player entries are rendered.
     *
     * @throws IllegalStateException if the service has already been started
     */
    @Synchronized
    fun start() {
        check(!started) { "The tablist was already started" }

        templates()

        started = true
        entries.start()
        TablistScheduler.startTask()
        refreshHeaderFooter()
        entries.updateAll()
    }

    /**
     * Stops the tab list service.
     *
     * The scheduler is cancelled and processing of new player entry updates is disabled. Calling this
     * method while the service is already stopped has no effect.
     */
    @Synchronized
    fun stop() {
        if (!started) return

        TablistScheduler.cancelTask()
        entries.stop()
        started = false
    }

    /**
     * Handles a player joining the server.
     *
     * The joining player's header and footer are rendered, the player-count-dependent content of all
     * viewers is invalidated, and the player's tab list entry is updated.
     *
     * @param viewer the player that joined
     */
    fun viewerJoined(viewer: TabPlayer) {
        TabPlatform.launch {
            additions.invalidatePlayer(viewer)
            additions.invalidateAll(TablistUpdateReason.PLAYER_COUNT)
            entries.updateEntry(viewer.uuid)
        }
    }

    /**
     * Handles a player leaving the server.
     *
     * Cached rendering state for the player is discarded and player-count-dependent content is
     * invalidated for the remaining viewers.
     *
     * @param viewerUuid the UUID of the player that left
     */
    fun viewerLeft(viewerUuid: UUID) {
        additions.forget(viewerUuid)
        entries.forget(viewerUuid)
        additions.invalidateAll(TablistUpdateReason.PLAYER_COUNT)
    }

    /**
     * Registers a custom [placeholder] for use in tab list templates.
     *
     * Event-based update conditions are installed immediately. Registering a placeholder invalidates
     * the cached template analysis and refreshes the header and footer when the service is running.
     *
     * @param placeholder the placeholder to register
     * @throws IllegalArgumentException if the placeholder name is not lowercase, conflicts with a
     * built-in placeholder, is already registered, or contains an unsupported event update condition
     */
    fun registerPlaceholder(placeholder: TabPlaceholder) {
        val name = placeholder.tagName

        require(name == name.lowercase()) { "Placeholder tag names are lower case, '$name' is not" }
        require(name !in builtins) { "'$name' is a placeholder the tablist fills in itself" }
        require(registered.putIfAbsent(name, placeholder) == null) {
            "A placeholder named '$name' is already registered"
        }

        eventTriggers[name] = UpdateCondition.flatten(placeholder.updates)
            .filterIsInstance<UpdateCondition.OnEvent<*>>()
            .map { condition -> installTrigger(condition) { invalidate(setOf(placeholder)) } }

        placeholderVersion.incrementAndGet()
        refreshHeaderFooter()
    }

    /**
     * Unregisters a previously registered [placeholder].
     *
     * Any event listeners installed for its [UpdateCondition.OnEvent] conditions are removed, cached
     * template analysis is invalidated, and the header and footer are refreshed when the service is
     * running.
     *
     * If the exact placeholder instance is not registered under its tag name, this method has no
     * effect.
     *
     * @param placeholder the placeholder to unregister
     */
    fun unregisterPlaceholder(placeholder: TabPlaceholder) {
        if (!registered.remove(placeholder.tagName, placeholder)) return

        eventTriggers.remove(placeholder.tagName)?.forEach(SurfEventBus::unregisterListeners)
        placeholderVersion.incrementAndGet()
        refreshHeaderFooter()
    }

    /**
     * Marks the specified [placeholders] as potentially changed.
     *
     * Only templates referencing at least one of the placeholders are considered for re-rendering.
     * Rendered content is applied only when it differs from the previously shown result.
     *
     * The request is ignored when [placeholders] is empty or the service has not been started.
     *
     * @param placeholders the placeholders whose values may have changed
     */
    fun invalidate(placeholders: Set<TabPlaceholder>) {
        if (placeholders.isEmpty() || !started) return

        additions.invalidateAll(TablistUpdateReason.Placeholders(placeholders))
    }

    /**
     * Requests a complete header and footer refresh for all viewers.
     *
     * The request is ignored until [start] has initialized the platform.
     */
    fun refreshHeaderFooter() {
        if (!started) return

        additions.invalidateAll(TablistUpdateReason.Configuration)
    }

    /**
     * Invalidates header and footer content for all viewers using the supplied [reason].
     *
     * The request is ignored while the service is stopped.
     *
     * @param reason the reason for invalidating the current rendered content
     */
    fun invalidateAll(reason: TablistUpdateReason) {
        if (!started) return

        additions.invalidateAll(reason)
    }

    /**
     * Installs an event bus listener for the supplied [condition].
     *
     * Synchronous and asynchronous Surf events are registered through their corresponding event bus
     * APIs. Matching events invoke [fire] at monitor priority.
     *
     * @param condition the event-based placeholder update condition
     * @param fire the action invoked when a matching event is received
     * @return the listener registration object required to unregister the handler later
     * @throws IllegalArgumentException if the configured event type is neither a [SurfSyncEvent] nor
     * a [SurfAsyncEvent]
     */
    @Suppress("UNCHECKED_CAST")
    private fun installTrigger(condition: UpdateCondition.OnEvent<*>, fire: () -> Unit): Any {
        val type = condition.type
        val matches = condition.matches as (SurfEvent) -> Boolean

        return when {
            SurfSyncEvent::class.java.isAssignableFrom(type) -> {
                SurfEventBus.registerHandler(
                    eventClass = type as Class<SurfSyncEvent>,
                    priority = SurfEventPriority.MONITOR,
                    ignoreCancelled = false,
                    handler = { event -> if (matches(event)) fire() }
                )
            }

            SurfAsyncEvent::class.java.isAssignableFrom(type) -> {
                SurfEventBus.registerAsyncHandler(
                    eventClass = type as Class<SurfAsyncEvent>,
                    priority = SurfEventPriority.MONITOR,
                    ignoreCancelled = false,
                    handler = { event -> if (matches(event)) fire() }
                )
            }

            else -> error("${type.name} is neither a sync nor an async surf event")
        }
    }

    /**
     * Requests a re-render of the tab list entry for the player identified by [uuid].
     *
     * @param uuid the UUID of the player whose entry should be updated
     */
    fun updateEntry(uuid: UUID) = entries.updateEntry(uuid)

    /**
     * Broadcasts an entry update request for the player identified by [uuid] across the network.
     *
     * The request is published through Redis and may be handled by every participating server.
     *
     * @param uuid the UUID of the player whose entry should be updated
     */
    fun broadcastEntryUpdate(uuid: UUID) {
        redisApi.publishEvent(TabEntryUpdateRedisEvent(uuid)).invokeOnCompletion { e ->
            if (e != null) {
                log.atWarning()
                    .withCause(e)
                    .log("Failed to broadcast tab entry update for player $uuid")
            }
        }
    }

    /**
     * Reloads the tab list configuration and refreshes all rendered content.
     *
     * Both header/footer content and every currently known player entry are requested for re-rendering.
     */
    fun reload() {
        TablistConfigFile.reloadFromFile()
        refreshHeaderFooter()
        entries.updateAll()
    }

    /**
     * Returns the currently configured tab list templates analyzed against the available placeholders.
     *
     * The previous analysis is reused while both the configured header/footer source and the set of
     * registered placeholders remain unchanged. Otherwise, the templates are analyzed again and the
     * result is cached.
     *
     * @return the analyzed tab list templates
     */
    fun templates(): TablistTemplates {
        val config = tablistConfig
        val version = placeholderVersion.get()
        val current = analyzed

        if (
            current != null &&
            current.version == version &&
            current.templates.matches(config.header, config.footer)
        ) {
            return current.templates
        }

        val templates = TablistTemplates.analyze(
            config.header,
            config.footer,
            miniMessage,
            ::resolvePlaceholder
        )
        analyzed = AnalyzedTemplates(templates, version)

        return templates
    }

    /**
     * Returns the fallback refresh interval for templates containing values whose changes cannot be
     * observed directly.
     *
     * @return the configured fallback refresh interval
     */
    fun refreshInterval(): Duration = tablistConfig.unknownPlaceholderRefreshSeconds.seconds

    private fun resolvePlaceholder(tagName: String): TabPlaceholder? {
        return builtins[tagName] ?: registered[tagName]
    }

    /**
     * Captures the current values of all placeholders referenced by [templates].
     *
     * Each placeholder is evaluated at most once for the snapshot. Failures are logged and the
     * corresponding value is omitted, allowing the remainder of the header or footer to render
     * normally. Coroutine cancellation is propagated unchanged.
     *
     * @param templates the templates whose referenced placeholder values should be captured
     * @return a new snapshot containing the resolved values and current online player count
     */
    private fun captureValues(templates: TablistTemplates): TablistValues {
        val needed = templates.placeholders
        val values = Object2ObjectOpenHashMap<TabPlaceholder, Component>(needed.size)

        for (placeholder in needed) {
            try {
                values[placeholder] = placeholder.value()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                log.atWarning()
                    .withCause(throwable)
                    .log("Failed to read tablist placeholder <%s>", placeholder.tagName)
            }
        }

        return TablistValues(generations.incrementAndGet(), values, TabPlatform.onlinePlayerCount())
    }

}
