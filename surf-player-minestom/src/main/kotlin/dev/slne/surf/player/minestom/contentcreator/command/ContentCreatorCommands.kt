package dev.slne.surf.social.minestom.contentcreator.command

import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.literalArgument
import dev.slne.surf.api.minestom.command.dsl.playerExecutor
import dev.slne.surf.social.api.contentcreator.platform.PlatformState
import dev.slne.surf.social.core.client.contentcreator.ContentCreatorPermissions
import dev.slne.surf.social.core.client.contentcreator.command.LiveStateDebugCommand

fun contentCreatorCommand() {
    commandTree("surfcontentcreator") {
        withPermission(ContentCreatorPermissions.COMMAND)

        literalArgument("debug") {
            literalArgument("live") {
                playerExecutor { player, _ ->
                    LiveStateDebugCommand.setTwitchState(
                        player,
                        player.uuid,
                        PlatformState.ONLINE
                    )
                }
            }

            literalArgument("offline") {
                playerExecutor { player, _ ->
                    LiveStateDebugCommand.setTwitchState(
                        player,
                        player.uuid,
                        PlatformState.OFFLINE
                    )
                }
            }
        }
    }
}
