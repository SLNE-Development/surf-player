package dev.slne.surf.player.paper.tab.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.tab.command.reloadTablist
import dev.slne.surf.player.core.client.tab.permission.TabPermissions
import dev.slne.surf.player.paper.command.PaperAdminAction
import kotlin.system.measureTimeMillis

/**
 * The actions of `/surfplayer tab`.
 */
fun tabAdminActions() = listOf(
    PaperAdminAction("reload", TabPermissions.ADMIN_RELOAD, "Lädt die Tablist-Konfiguration neu") {
        anyExecutor { sender, _ ->
            val millis = measureTimeMillis { reloadTablist() }
            sender.sendMessage(CommandMessages.configReloaded("tab", millis))
        }
    },
)
