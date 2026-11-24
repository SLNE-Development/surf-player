package dev.slne.surf.social.minestom.profile

import com.google.auto.service.AutoService
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.api.minestom.inventory.framework.open
import dev.slne.surf.social.core.client.profile.platform.ProfilePlatform
import dev.slne.surf.social.minestom.profile.menu.own.ownProfileMenu
import java.util.*

@AutoService(ProfilePlatform::class)
class MinestomProfilePlatform : ProfilePlatform {
    override fun openOwnProfileMenu(playerUuid: UUID) {
        val player = ConnectionManager.getOnlinePlayerByUuid(playerUuid) ?: return
        player.scheduleNextTick { ownProfileMenu.open(player) }
    }
}
