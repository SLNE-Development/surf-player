package dev.slne.surf.player.core.client.tab.entry

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.player.api.tab.event.TabEntryRenderEvent
import dev.slne.surf.player.core.client.contentcreator.LiveTagRenderer
import dev.slne.surf.player.core.client.playtime.afk.AfkService
import dev.slne.surf.player.core.client.tab.hook.ClanHook
import dev.slne.surf.player.core.client.tab.hook.LuckPermsHook
import dev.slne.surf.player.core.client.tab.platform.TabPlayer
import dev.slne.surf.player.core.client.tab.platform.TabPlatform
import dev.slne.surf.player.core.client.tab.service.UpdateCoalescer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

private val log = logger()

/**
 * The tab list order of a player, as surf-tab computed it.
 */
object TabOrder {
    /**
     * Added to the order of vanished players, so viewers allowed to see them find them at the top
     * of the tab list (a higher order is listed first). Must exceed every LuckPerms weight.
     */
    const val VANISHED_ORDER_OFFSET = 1_000_000

    /**
     * The order passed to [TabEntryRenderEvent]: the LuckPerms weight [lpWeight] (negative weights
     * unchanged), plus [VANISHED_ORDER_OFFSET] if the player is [vanished].
     */
    fun of(lpWeight: Int, vanished: Boolean): Int =
        if (vanished) lpWeight + VANISHED_ORDER_OFFSET else lpWeight
}

private val entryUpdateTimeout = 5.seconds
private val clanLookupTimeout = 2.seconds

private val afkTag = buildText {
    appendSpace()
    darkSpacer("[")
    spacer("AFK")
    darkSpacer("]")
}

private val vanishTag = buildText {
    darkSpacer("[")
    error("SPEC")
    darkSpacer("]")
    appendSpace()
}

/**
 * Manages the rendered tab list entries for players connected to this server.
 *
 * Each render creates and fires a [TabEntryRenderEvent], allowing other plugins to modify the
 * player's displayed name and ordering or take over rendering entirely by cancelling the event.
 *
 * Entries are initially rendered without clan information so they can be displayed immediately.
 * The clan tag is then resolved asynchronously and the entry is updated again if necessary.
 *
 * Optional decorations are resolved independently. A failure while resolving one decoration is
 * logged and replaced by its fallback value without preventing the remaining entry from rendering.
 *
 * The last successfully rendered state of each player is retained to avoid applying identical
 * updates. Concurrent update requests for the same player are coalesced by [UpdateCoalescer].
 */
class TabEntries {

    /**
     * Stores the last entry state applied for each player to suppress unchanged updates.
     */
    private val shown = ConcurrentHashMap<UUID, ShownEntry>()

    @Volatile
    private var started = false

    private val updates = UpdateCoalescer<UUID>(
        runUpdates = { block -> TabPlatform.launch { block() } },
        updateTimeout = entryUpdateTimeout,
        onTimeout = { uuid, _ ->
            log.atWarning().log("Tablist entry update for %s timed out", uuid)
        },
        update = { uuid -> render(uuid) }
    )

    /**
     * Enables processing of entry update requests.
     */
    fun start() {
        started = true
    }

    /**
     * Disables processing of new entry update requests.
     *
     * Requests made through [updateEntry] while this instance is stopped are ignored.
     */
    fun stop() {
        started = false
    }

    /**
     * Requests a re-render of the tab list entry for the player identified by [uuid].
     *
     * Repeated requests for the same player may be coalesced into a single update. The request is
     * ignored while this instance is stopped.
     *
     * @param uuid the UUID of the player whose entry should be updated
     */
    fun updateEntry(uuid: UUID) {
        if (!started) return

        updates.request(uuid, uuid)
    }

    /**
     * Requests a re-render of every currently online player's tab list entry.
     */
    fun updateAll() {
        for (player in TabPlatform.onlinePlayers()) {
            updateEntry(player.uuid)
        }
    }

    /**
     * Forgets the previously rendered state associated with [uuid].
     *
     * This should be called when a player leaves so a later entry for the same UUID is not compared
     * against stale state.
     *
     * @param uuid the UUID of the player whose cached state should be removed
     */
    fun forget(uuid: UUID) {
        shown.remove(uuid)
    }

    private suspend fun render(uuid: UUID) {
        val player = TabPlatform.player(uuid) ?: return
        val vanished = TabPlatform.isVanished(uuid)

        val event = TabEntryRenderEvent(
            uuid,
            name = if (vanished) {
                player.baseNameSnapshot().darkenColors()
            } else {
                player.baseNameSnapshot()
            },
            // Higher orders are listed first: vanished players at the top, as in surf-tab. Paper
            // only accepts non-negative orders; PaperTabPlayer clamps when applying the order.
            order = part("order", uuid, 0) {
                TabOrder.of(LuckPermsHook.getWeight(uuid), vanished)
            }
        )

        if (!event.call()) {
            return
        }

        if (!event.decorate) {
            show(player, event.name, event.order)
            return
        }

        show(player, decorate(event.name, uuid, clan = null), event.order)

        resolveClan(uuid)?.let { clan ->
            show(player, decorate(event.name, uuid, clan), event.order)
        }
    }

    private suspend fun show(player: TabPlayer, name: Component, order: Int) {
        val next = ShownEntry(player.audience, name, order)

        if (shown.put(player.uuid, next)?.sameAs(next) == true) return

        player.showTabEntry(name, order)
    }

    private fun decorate(name: Component, uuid: UUID, clan: Component?) = buildText {
        append(part("vanish", uuid, Component.empty()) { vanishTag(uuid) })
        append(name)
        clan?.let { append(it) }
        append(part("live", uuid, Component.empty()) { liveTag(uuid) })
        append(part("afk", uuid, Component.empty()) { afkTag(uuid) })
    }

    private suspend fun resolveClan(uuid: UUID): Component? = try {
        withTimeout(clanLookupTimeout) {
            ClanHook.getClanTag(uuid)?.let { tag ->
                buildText {
                    appendSpace()
                    append(tag)
                }
            }
        }
    } catch (timeout: TimeoutCancellationException) {
        log.atWarning().withCause(timeout).log("Clan lookup for tablist entry %s timed out", uuid)
        null
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        log.atWarning().withCause(throwable)
            .log("Failed to resolve clan tag for tablist entry %s", uuid)
        null
    }

    private fun vanishTag(uuid: UUID): TextComponent {
        return if (TabPlatform.isVanished(uuid)) vanishTag else Component.empty()
    }

    private fun liveTag(uuid: UUID): Component = LiveTagRenderer.render(uuid, space = true)

    private fun afkTag(uuid: UUID): TextComponent {
        return if (AfkService.isAfk(uuid)) afkTag else Component.empty()
    }

    /**
     * Resolves an optional part of a tab list entry while isolating failures from the remaining
     * render operation.
     *
     * Non-cancellation failures are logged and result in [fallback]. Coroutine cancellation is
     * propagated unchanged.
     *
     * @param what a human-readable identifier used when logging resolution failures
     * @param uuid the UUID of the entry being rendered
     * @param fallback the value returned when [value] fails
     * @param value resolves the requested entry part
     * @return the resolved value, or [fallback] if resolution fails
     */
    private inline fun <R> part(what: String, uuid: UUID, fallback: R, value: () -> R): R = try {
        value()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        log.atWarning().withCause(throwable)
            .log("Failed to resolve tablist part %s for entry %s", what, uuid)
        fallback
    }

    // factor: higher = lighter, lower = darker
    private fun Component.darkenColors(factor: Double = 0.6): Component {
        val color = style().color()
        val newColor = color?.let {
            TextColor.color(
                (it.red() * factor).toInt(),
                (it.green() * factor).toInt(),
                (it.blue() * factor).toInt()
            )
        }

        return children(
            children().map { it.darkenColors(factor) }
        ).style(
            style().color(newColor)
        )
    }
}
