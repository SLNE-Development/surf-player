package dev.slne.surf.social.paper.profile.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.social.core.client.profile.permission.ProfilePermissions
import dev.slne.surf.social.paper.profile.menu.own.ownProfileMenu

fun profileCommand() = commandTree("profile") {
    withPermission(ProfilePermissions.COMMAND_PROFILE)
    playerExecutor { player, _ ->
        ownProfileMenu.open(player)
    }
}
