package dev.slne.surf.player.core.client.contentcreator.service

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.api.core.util.toObjectSet
import dev.slne.surf.player.api.contentcreator.ContentCreator
import dev.slne.surf.player.api.contentcreator.StreamingPlatform
import dev.slne.surf.player.api.contentcreator.listener.StateChangeListener
import dev.slne.surf.player.api.contentcreator.platform.PlatformState
import dev.slne.surf.player.api.contentcreator.platform.PlatformType
import dev.slne.surf.player.core.client.contentcreator.CoreContentCreator
import dev.slne.surf.player.api.connection.SurfConnectionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

object ContentCreatorService {

    private val log = logger()

    private val stateChangeListeners = CopyOnWriteArraySet<StateChangeListener>()

    private val coreCreators = ConcurrentHashMap<UUID, CoreContentCreator>()
    private val onlineSessions = ConcurrentHashMap.newKeySet<UUID>()

    val contentCreators get() = coreCreators.values.toObjectSet<ContentCreator>()

    /**
     * Marks the session of [playerUuid] as active. Must be called on the connection thread before
     * the asynchronous [cacheCreator] is started, so that [endSession] can always cancel it out.
     */
    fun beginSession(playerUuid: UUID) {
        onlineSessions.add(playerUuid)
    }

    /**
     * Ends the session of [playerUuid] and returns the creator that was cached for it, if any, so
     * that the caller can still clean up platform listeners for a player that is already gone.
     */
    fun endSession(playerUuid: UUID): CoreContentCreator? {
        onlineSessions.remove(playerUuid)
        return coreCreators.remove(playerUuid)
    }

    suspend fun cacheCreator(playerUuid: UUID) = SurfConnectionApi.findTwitchConnection(playerUuid)?.let {
        val creator = CoreContentCreator(playerUuid).also { c ->
            c.twitchName = it.twitchName
        }

        coreCreators[playerUuid] = creator

        if (playerUuid !in onlineSessions) {
            coreCreators.remove(playerUuid, creator)
        }
    }

    fun invalidate(playerUuid: UUID) {
        endSession(playerUuid)
    }

    /**
     * Forces the state of the [platformType] platform of the content creator behind [playerUuid] to
     * [newState] and notifies every registered state change listener.
     *
     * @return `false` if that player is no known content creator or has no such platform
     */
    fun updatePlatformState(
        playerUuid: UUID,
        platformType: PlatformType,
        newState: PlatformState
    ): Boolean {
        val platform = getContentCreator(playerUuid)?.getStreamingPlatform(platformType) ?: return false

        platform.state = newState
        callOnStateChangeListener(playerUuid, platform, newState)

        return true
    }

    fun registerStateChangeListener(listener: StateChangeListener) {
        stateChangeListeners.add(listener)
    }

    /**
     * Notifies every registered [StateChangeListener] that [streamingPlatform] of [playerUuid]
     * changed to [newState]. A listener that throws does not stop the others.
     */
    fun callOnStateChangeListener(
        playerUuid: UUID,
        streamingPlatform: StreamingPlatform,
        newState: PlatformState
    ) {
        for (listener in stateChangeListeners) {
            try {
                listener.onStateChanged(playerUuid, streamingPlatform, newState)
            } catch (e: Exception) {
                log.atWarning()
                    .withCause(e)
                    .log(
                        "State change listener ${listener.javaClass.name} threw while handling " +
                                "$newState for $playerUuid."
                    )
            }
        }
    }

    /**
     * Drops all cached player state.
     */
    fun clear() {
        onlineSessions.clear()
        coreCreators.clear()
    }

    /**
     * Whether the session of [playerUuid] is still active.
     */
    fun isSessionActive(playerUuid: UUID): Boolean = playerUuid in onlineSessions

    fun getContentCreator(uuid: UUID): ContentCreator? = coreCreators[uuid]

    /**
     * Returns the creator whose [platformType] platform is named [platformName], or `null` if no
     * cached creator matches.
     */
    fun findByPlatformName(platformType: PlatformType, platformName: String): ContentCreator? {
        for (creator in coreCreators.values) {
            val platform = creator.getStreamingPlatform(platformType) ?: continue
            if (platform.name.equals(platformName, ignoreCase = true)) {
                return creator
            }
        }

        return null
    }

    fun getContentCreators(uuids: ObjectSet<UUID>): ObjectSet<ContentCreator> = uuids.mapNotNullTo(
        mutableObjectSetOf()
    ) { getContentCreator(it) }
}
