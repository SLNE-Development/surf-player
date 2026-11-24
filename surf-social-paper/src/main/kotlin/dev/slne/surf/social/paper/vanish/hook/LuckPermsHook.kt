package dev.slne.surf.social.paper.vanish.hook

import net.luckperms.api.LuckPermsProvider
import org.bukkit.entity.Player

object LuckPermsHook {
    private val luckPerms by lazy {
        LuckPermsProvider.get()
    }

    fun getPrefix(player: Player) =
        luckPerms.userManager.getUser(player.uniqueId)?.cachedData?.metaData?.prefix ?: ""
}