package dev.slne.surf.social.core.client.util

import dev.slne.surf.api.core.luckperms.LuckPermsAccess
import dev.slne.surf.api.core.luckperms.prefix
import dev.slne.surf.api.core.minimessage.miniMessage
import net.kyori.adventure.text.Component
import java.util.*

/**
 * The LuckPerms prefix of the player identified by [uuid] as MiniMessage, or an empty string when
 * they have none or are not loaded.
 */
fun luckPermsPrefix(uuid: UUID): String = LuckPermsAccess.getUser(uuid)?.prefix ?: ""

/**
 * [name] preceded by the LuckPerms prefix of the player identified by [uuid].
 */
fun prefixedName(uuid: UUID, name: String): Component =
    miniMessage.deserialize(luckPermsPrefix(uuid) + name)
