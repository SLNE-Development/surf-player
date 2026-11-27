package dev.slne.surf.player.minestom.profile

import dev.slne.surf.api.minestom.inventory.framework.register
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.profile.command.profileCommand
import dev.slne.surf.player.minestom.profile.menu.friends.friendDetailView
import dev.slne.surf.player.minestom.profile.menu.friends.friendRemoveView
import dev.slne.surf.player.minestom.profile.menu.friends.friendRequestsView
import dev.slne.surf.player.minestom.profile.menu.friends.friendsView
import dev.slne.surf.player.minestom.profile.menu.friends.sentFriendRequestsView
import dev.slne.surf.player.minestom.profile.menu.own.ownProfileMenu

/**
 * The profile menu (formerly the `surf-profile-minestom` plugin).
 */
object ProfileMinestomFeature : MinestomPlayerFeature {
    override val id = "profile"

    override suspend fun onLoad() {
        ownProfileMenu.register()
        friendsView.register()
        friendRequestsView.register()
        sentFriendRequestsView.register()
        friendDetailView.register()
        friendRemoveView.register()
    }

    override suspend fun onEnable() {
        profileCommand()
    }
}
