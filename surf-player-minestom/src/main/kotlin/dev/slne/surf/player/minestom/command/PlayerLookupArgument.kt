package dev.slne.surf.social.minestom.command

import dev.slne.surf.api.minestom.command.argument.CustomArgument
import dev.slne.surf.api.minestom.command.argument.StringArgument
import dev.slne.surf.api.minestom.command.suggestion.ArgumentSuggestions
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.minestom.server.entity.Player
import java.util.*

/**
 * Looks up a [T] by the typed name in relation to the player running the command, e.g. one of
 * their friends or clan members. Resolves to `null` when nothing matches or the sender is no
 * player; suggests the names [suggestions] returns for the sending player.
 */
class PlayerLookupArgument<T>(
    nodeName: String,
    lookup: suspend (senderUuid: UUID, input: String) -> T?,
    suggestions: suspend (senderUuid: UUID) -> Collection<String>
) : CustomArgument<Deferred<T?>, String>(StringArgument(nodeName), { info ->
    val senderUuid = (info.sender as? Player)?.uuid
    val input = info.currentInput

    commandArgumentScope.async { senderUuid?.let { lookup(it, input) } }
}) {

    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync { info ->
            val player = info.sender as? Player ?: return@stringCollectionAsync emptyList()

            suggestions(player.uuid)
        })
    }
}
