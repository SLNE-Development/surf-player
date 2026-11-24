package dev.slne.surf.player.minestom.tab.command

import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.literalArgument
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
