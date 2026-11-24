package dev.slne.surf.player.minestom.profile

import dev.slne.surf.api.minestom.inventory.framework.register
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.profile.command.profileCommand
import dev.slne.surf.player.minestom.profile.menu.own.ownProfileMenu

/**
 * The profile menu (formerly the `surf-profile-minestom` plugin).
 */
object ProfileMinestomFeature : MinestomPlayerFeature {
    override val id = "profile"

    override suspend fun onLoad() {
        ownProfileMenu.register()
    }

    override suspend fun onEnable() {
        profileCommand()
    }
}
