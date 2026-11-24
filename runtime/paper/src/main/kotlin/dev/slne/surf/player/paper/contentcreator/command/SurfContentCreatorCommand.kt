package dev.slne.surf.player.paper.contentcreator.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.player.api.contentcreator.platform.PlatformState
import dev.slne.surf.player.core.client.contentcreator.ContentCreatorPermissions
import dev.slne.surf.player.core.client.contentcreator.command.LiveStateDebugCommand

fun surfContentCreatorCommand() = commandTree("surfcontentcreator") {
    withPermission(ContentCreatorPermissions.COMMAND)

    literalArgument("debug") {
        literalArgument("live") {
            playerExecutor { player, _ ->
                LiveStateDebugCommand.setTwitchState(
                    player,
                    player.uniqueId,
                    PlatformState.ONLINE
                )
            }
        }

        literalArgument("offline") {
            playerExecutor { player, _ ->
                LiveStateDebugCommand.setTwitchState(
                    player,
                    player.uniqueId,
                    PlatformState.OFFLINE
                )
            }
        }
    }
}
