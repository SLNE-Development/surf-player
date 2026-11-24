package dev.slne.surf.player.velocity.friends.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.friends.redis.event.FriendConnectionRedisEvent
import dev.slne.surf.player.velocity.plugin

/**
 * Publishes [FriendConnectionRedisEvent] for network joins (the first server connection) and
 * network quits; server switches publish nothing.
 */
object FriendNetworkConnectionListener {
    @Subscribe
    fun onServerConnected(event: ServerConnectedEvent) {
        if (event.previousServer.isPresent) return

        publish(
            FriendConnectionRedisEvent(
                playerUuid = event.player.uniqueId,
                online = true,
                serverName = event.server.serverInfo.name,
            )
        )
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        // Only players that were announced as online are announced as offline.
        if (event.loginStatus != DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) return

        publish(FriendConnectionRedisEvent(playerUuid = event.player.uniqueId, online = false))
    }

    private fun publish(event: FriendConnectionRedisEvent) {
        SurfPlayerClient.redisApi.publishEvent(event).invokeOnCompletion { error ->
            if (error != null) {
                plugin.logger.warn("Failed to publish friend connection of ${event.playerUuid}", error)
            }
        }
    }
}
