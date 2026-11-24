package dev.slne.surf.player.minestom.chat.message

import net.minestom.server.entity.Player
import java.util.regex.Pattern

/**
 * The online players a chat message can mention, as of [generation].
 *
 * @property pattern matches any online player's name, `null` when nobody is online
 * @property playersByName the online players by their lowercase name
 */
internal class MentionCache(
    val generation: Long,
    val pattern: Pattern?,
    val playersByName: Map<String, Player>
)
