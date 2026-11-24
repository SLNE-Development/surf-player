package dev.slne.surf.social.paper.tab.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.social.core.client.tab.command.reloadTablist
import dev.slne.surf.social.core.client.tab.command.sendTablistReloaded
import dev.slne.surf.social.core.client.tab.permission.TabPermissions

fun surfTabCommand() = commandTree("surftab") {
    withPermission(TabPermissions.COMMAND_SURFTAB)

    literalArgument("reload") {
        anyExecutor { executor, _ ->
            reloadTablist()

            executor.sendTablistReloaded()
        }
    }
}
