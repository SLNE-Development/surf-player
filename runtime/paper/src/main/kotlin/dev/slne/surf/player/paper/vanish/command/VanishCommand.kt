package dev.slne.surf.player.paper.vanish.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.player.paper.vanish.config.reloadVanishConfig
import dev.slne.surf.player.paper.vanish.service.VanishService
import dev.slne.surf.player.paper.vanish.util.VanishPermissionRegistry
import dev.slne.surf.player.paper.vanish.util.isVanished
import kotlin.system.measureTimeMillis

fun vanishCommand() = commandTree("vanish") {
    withPermission(VanishPermissionRegistry.VANISH_COMMAND)

    playerExecutor { player, _ ->
        if (player.isVanished) {
            VanishService.reappear(player)
            player.sendText {
                appendSuccessPrefix()
                success("Du bist nun sichtbar.")
            }
        } else {
            VanishService.vanish(player)
            player.sendText {
                appendNewInfoPrefixedLine()
                darkSpacer("-".repeat(25))

                appendNewInfoPrefixedLine()

                appendNewInfoPrefixedLine()
                info("Du bist nun unsichtbar!")

                appendNewInfoPrefixedLine()

                appendNewInfoPrefixedLine()
                spacer("Benutze /vanish spectate ".toSmallCaps())
                appendNewInfoPrefixedLine()
                spacer("um anderen Spielern zuzuschauen.".toSmallCaps())

                appendNewInfoPrefixedLine()
                darkSpacer("-".repeat(25))
            }
        }
    }

    literalArgument("spectate") {
        playerExecutor { player, _ ->
            if (VanishService.isSpectating(player.uniqueId)) {
                VanishService.stopSpectateMode(player)
                player.sendText {
                    appendSuccessPrefix()
                    success("Du bist nun nicht mehr im SpectateMode.")
                }
            } else {
                VanishService.startSpectateMode(player)
                player.sendText {
                    appendSuccessPrefix()
                    success("Du bist nun im SpectateMode.")
                }
            }
        }
    }

    literalArgument("reload") {
        withPermission(VanishPermissionRegistry.VANISH_COMMAND_RELOAD)
        anyExecutor { executor, _ ->
            val ms = measureTimeMillis {
                reloadVanishConfig()
            }

            executor.sendText {
                appendSuccessPrefix()
                success("Die Konfiguration wurde neu geladen (${ms}ms)!")
            }
        }
    }
}