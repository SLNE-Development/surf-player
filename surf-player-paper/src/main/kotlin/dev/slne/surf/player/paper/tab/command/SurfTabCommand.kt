package dev.slne.surf.player.paper.tab.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.player.core.client.tab.command.reloadTablist
import dev.slne.surf.player.core.client.tab.command.sendTablistReloaded
import dev.slne.surf.player.core.client.tab.permission.TabPermissions

fun surfTabCommand() = commandTree("surftab") {
    withPermission(TabPermissions.COMMAND_SURFTAB)

    literalArgument("reload") {
        anyExecutor { executor, _ ->
            reloadTablist()

            executor.sendTablistReloaded()
        }
    }
}
