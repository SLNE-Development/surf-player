package dev.slne.surf.social.paper.profile.platform

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.auto.service.AutoService
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.social.core.client.profile.platform.ProfilePlatform
import dev.slne.surf.social.paper.profile.menu.own.ownProfileMenu
import dev.slne.surf.social.paper.profile.plugin
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import java.util.*

@AutoService(ProfilePlatform::class)
class PaperProfilePlatform : ProfilePlatform, Services.Fallback {
    override fun openOwnProfileMenu(playerUuid: UUID) {
        val player = Bukkit.getPlayer(playerUuid) ?: return
        plugin.launch(plugin.entityDispatcher(player)) {
            ownProfileMenu.open(player)
        }
    }
}
