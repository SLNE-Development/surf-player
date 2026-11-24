package dev.slne.surf.player.paper.contentcreator.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.player.api.contentcreator.platform.PlatformState
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.contentcreator.command.LiveStateDebugCommand
import dev.slne.surf.player.core.client.contentcreator.config.reloadContentCreatorConfig
import dev.slne.surf.player.core.client.contentcreator.permission.ContentCreatorPermissions
import dev.slne.surf.player.paper.command.PaperAdminAction
import kotlin.system.measureTimeMillis

/**
 * The actions of `/surfplayer contentcreator`.
 */
fun contentCreatorAdminActions() = listOf(
    PaperAdminAction("reload", ContentCreatorPermissions.ADMIN_RELOAD, "Lädt die Content-Creator-Konfiguration neu") {
        anyExecutor { sender, _ ->
            val millis = measureTimeMillis { reloadContentCreatorConfig() }
            sender.sendMessage(CommandMessages.configReloaded("contentcreator", millis))
        }
    },

    PaperAdminAction(
        "debug",
        ContentCreatorPermissions.ADMIN_DEBUG,
        "Setzt deinen eigenen Twitch-Live-Status zum Testen",
        "<live|offline>"
    ) {
        subcommand("live") {
            playerExecutor { player, _ ->
                LiveStateDebugCommand.setTwitchState(player, player.uniqueId, PlatformState.ONLINE)
            }
        }
        subcommand("offline") {
            playerExecutor { player, _ ->
                LiveStateDebugCommand.setTwitchState(player, player.uniqueId, PlatformState.OFFLINE)
            }
        }
    },
)
