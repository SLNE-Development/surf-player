package dev.slne.surf.social.core.client.nametag.hook

import dev.slne.surf.social.core.client.nametag.platform.NametagPlatform
import dev.slne.surf.social.core.client.nametag.service.NametagService
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User

object LuckPermsHook {
    val luckPerms by lazy {
        LuckPermsProvider.get()
    }

    fun updatePlayer(user: User) {
        val player = NametagPlatform.player(user.uniqueId) ?: return

        NametagPlatform.launch {
            NametagService.handleDataUpdate(player)
        }
    }
}
