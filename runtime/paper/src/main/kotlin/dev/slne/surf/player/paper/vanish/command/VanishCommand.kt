package dev.slne.surf.player.paper.vanish.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.paper.command.PaperAdminAction
import dev.slne.surf.player.paper.vanish.config.reloadVanishConfig
import dev.slne.surf.player.core.client.vanish.permission.VanishPermissions
import dev.slne.surf.player.paper.vanish.service.VanishService
import dev.slne.surf.player.paper.vanish.util.isVanished
import kotlin.system.measureTimeMillis

/**
 * `/vanish` toggles vanish, `/vanish spectate` the spectate mode.
 */
fun vanishCommand() = commandAPICommand("vanish") {
    withPermission(VanishPermissions.TEAM_VANISH)

    playerExecutor { player, _ ->
        if (player.isVanished) {
            VanishService.reappear(player)
            player.sendText {
                appendSuccessPrefix()
                success("Du bist nun sichtbar.")
            }
            return@playerExecutor
        }

        VanishService.vanish(player)
        player.sendText {
            appendSuccessPrefix()
            success("Du bist nun unsichtbar.")
            appendNewline()
            appendInfoPrefix()
            spacer("Mit /vanish spectate kannst du anderen Spielern zuschauen.".toSmallCaps())
        }
    }

    subcommand("spectate") {
        playerExecutor { player, _ ->
            if (VanishService.isSpectating(player.uniqueId)) {
                VanishService.stopSpectateMode(player)
                player.sendText {
                    appendSuccessPrefix()
                    success("Du schaust nun niemandem mehr zu.")
                }
            } else {
                VanishService.startSpectateMode(player)
                player.sendText {
                    appendSuccessPrefix()
                    success("Du bist nun im Zuschauermodus.")
                }
            }
        }
    }
}

/**
 * The actions of `/surfplayer vanish`.
 */
fun vanishAdminActions() = listOf(
    PaperAdminAction("reload", VanishPermissions.ADMIN_RELOAD, "Lädt die Vanish-Konfiguration neu") {
        anyExecutor { sender, _ ->
            val millis = measureTimeMillis { reloadVanishConfig() }
            sender.sendMessage(CommandMessages.configReloaded("vanish", millis))
        }
    },
)
