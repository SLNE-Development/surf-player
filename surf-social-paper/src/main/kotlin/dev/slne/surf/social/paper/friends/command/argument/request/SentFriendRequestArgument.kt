package dev.slne.surf.social.paper.friends.command.argument.request

import com.github.shynixn.mccoroutine.folia.scope
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.api.core.messages.adventure.uuid
import dev.slne.surf.api.core.messages.adventure.uuidOrNull
import dev.slne.surf.api.paper.command.args.SuspendCustomArgument
import dev.slne.surf.social.api.friends.player.FriendsPlayer
import dev.slne.surf.social.core.client.friends.command.NO_SENT_FRIEND_REQUEST_MESSAGE
import dev.slne.surf.social.core.client.friends.command.findSentFriendRequestTarget
import dev.slne.surf.social.core.client.friends.command.sentFriendRequestNames
import dev.slne.surf.social.paper.plugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future

class SentFriendRequestArgument(nodeName: String) :
    SuspendCustomArgument<FriendsPlayer, String>(StringArgument(nodeName)) {

    override suspend fun CoroutineScope.parse(info: CustomArgumentInfo<String>): FriendsPlayer {
        return findSentFriendRequestTarget(info.sender.uuid(), info.currentInput)
            ?: throw CustomArgumentException.fromAdventureComponent(NO_SENT_FRIEND_REQUEST_MESSAGE)
    }

    init {
        replaceSuggestions(
            ArgumentSuggestions.stringsAsync { info ->
                plugin.scope.future {
                    val uuid = info.sender.uuidOrNull() ?: return@future arrayOf()

                    sentFriendRequestNames(uuid).toTypedArray()
                }
            }
        )
    }
}

fun CommandAPICommand.sentFriendRequestArgument(
    nodeName: String
): CommandAPICommand = withArguments(SentFriendRequestArgument(nodeName))
