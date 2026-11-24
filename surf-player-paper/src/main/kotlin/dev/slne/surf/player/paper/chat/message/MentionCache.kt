package dev.slne.surf.social.paper.chat.message

import org.bukkit.entity.Player
import java.util.regex.Pattern

/**
 * Snapshot of the online players that can be mentioned, valid for one cache [generation].
 *
 * @property pattern matches any online player's name, `null` if nobody is online
 * @property playersByName online players keyed by their lowercase name
 */
internal class MentionCache(
    val generation: Long,
    val pattern: Pattern?,
    val playersByName: Map<String, Player>
)
