package dev.slne.surf.social.core.client.chat.hook

import net.luckperms.api.LuckPermsProvider
import java.util.UUID

object LuckPermsHook {
    private val luckPerms by lazy {
        LuckPermsProvider.get()
    }

    fun getPrefix(uuid: UUID) =
        luckPerms.userManager.getUser(uuid)?.cachedData?.metaData?.prefix ?: ""
}
