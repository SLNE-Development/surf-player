package dev.slne.surf.social.minestom.friends.command.argument.friend

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.argument.Argument
import dev.slne.surf.api.minestom.command.argument.CustomArgument
import dev.slne.surf.api.minestom.command.argument.StringArgument
import dev.slne.surf.api.minestom.command.suggestion.ArgumentSuggestions
import dev.slne.surf.social.api.friends.player.FriendsPlayer
import dev.slne.surf.social.core.client.friends.command.findFriend
import dev.slne.surf.social.core.client.friends.command.friendNames
import dev.slne.surf.social.minestom.friends.command.friendArgumentScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.minestom.server.entity.Player

class FriendArgument(nodeName: String) :
    CustomArgument<Deferred<FriendsPlayer?>, String>(StringArgument(nodeName), { info ->
        val senderUuid = (info.sender as? Player)?.uuid
        val input = info.currentInput

        friendArgumentScope.async { senderUuid?.let { findFriend(it, input) } }
    }) {

    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync { info ->
            val player = info.sender as? Player ?: return@stringCollectionAsync emptyList()

            friendNames(player.uuid)
        })
    }
}

inline fun CommandAPICommand.friendArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<Deferred<FriendsPlayer?>>.() -> Unit = {}
): CommandAPICommand =
    withArguments(FriendArgument(nodeName).setOptional(optional).apply(block))
