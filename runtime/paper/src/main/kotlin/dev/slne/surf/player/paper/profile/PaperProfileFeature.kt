package dev.slne.surf.player.paper.profile

import dev.slne.surf.api.paper.inventory.framework.register
import dev.slne.surf.player.paper.PaperPlayerFeature
import dev.slne.surf.player.paper.profile.command.profileCommand
import dev.slne.surf.player.paper.profile.menu.friends.friendDetailView
import dev.slne.surf.player.paper.profile.menu.friends.friendRemoveView
import dev.slne.surf.player.paper.profile.menu.friends.friendRequestsView
import dev.slne.surf.player.paper.profile.menu.friends.friendsView
import dev.slne.surf.player.paper.profile.menu.friends.sentFriendRequestsView
import dev.slne.surf.player.paper.profile.menu.own.ownProfileMenu

/**
 * The profile menu (formerly the `surf-profile-paper` plugin).
 */
object PaperProfileFeature : PaperPlayerFeature {
    override val name = "profile"

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
