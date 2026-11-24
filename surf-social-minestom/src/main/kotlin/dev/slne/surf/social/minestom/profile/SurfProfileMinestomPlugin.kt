package dev.slne.surf.social.minestom.profile

import dev.slne.surf.api.minestom.inventory.framework.register
import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.social.minestom.profile.command.profileCommand
import dev.slne.surf.social.minestom.profile.menu.own.ownProfileMenu

object SurfProfileMinestomPlugin : SurfMinestomPlugin() {
    override suspend fun onLoad() {
        ownProfileMenu.register()
    }

    override suspend fun onEnable() {
        profileCommand()
    }
}
