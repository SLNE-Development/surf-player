package dev.slne.surf.player.minestom.profile.command

import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.playerExecutor
import dev.slne.surf.api.minestom.inventory.framework.open
import dev.slne.surf.player.core.client.profile.permission.ProfilePermissions
import dev.slne.surf.player.minestom.profile.menu.own.ownProfileMenu

fun profileCommand() = commandTree("profile") {
    withPermission(ProfilePermissions.COMMAND_PROFILE)
    playerExecutor { player, _ ->
        ownProfileMenu.open(player)
    }
}
