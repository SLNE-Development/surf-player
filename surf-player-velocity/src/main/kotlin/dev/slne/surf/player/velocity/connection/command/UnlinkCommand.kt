package dev.slne.surf.player.velocity.connection.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.velocity.command.executors.playerExecutorSuspend
import dev.slne.surf.player.core.client.connection.service.SocialConnectionsService
import dev.slne.surf.player.core.common.connection.rabbit.rpc.UnlinkResult
import dev.slne.surf.player.velocity.connection.permission.SocialPermissions
import dev.slne.surf.player.velocity.connection.redis.DiscordRoleAction
import dev.slne.surf.player.velocity.connection.redis.DiscordRolesPublisher
import dev.slne.surf.player.velocity.plugin
import kotlinx.coroutines.CancellationException

fun unlinkCommand() = commandTree("unlink") {
    withPermission(SocialPermissions.COMMAND_UNLINK)

    playerExecutorSuspend { player, _ ->
        val result = SocialConnectionsService.unlinkMinecraftAccount(player.uniqueId)

        if (result == UnlinkResult.NotLinked) {
            player.sendText {
                appendErrorPrefix()
                error("Du hast keinen verknüpften Account.")
            }

            return@playerExecutorSuspend
        }

        require(result is UnlinkResult.Unlinked) {
            "Unexpected result type: ${result::class.java.name}"
        }

        result.discordId?.let { revokeDiscordRoles(it) }

        player.sendText {
            appendSuccessPrefix()
            success("Dein Minecraft Account wurde entkoppelt.")
        }
    }
}

/**
 * Asks surf-discord to remove the roles of the Discord user [discordId]; a failure is logged since
 * the account is unlinked either way.
 */
private suspend fun revokeDiscordRoles(discordId: Long) {
    try {
        DiscordRolesPublisher.publish(DiscordRoleAction.REMOVE, discordId)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (exception: Exception) {
        plugin.logger.error("Failed to publish the discord role removal for user $discordId", exception)
    }
}
