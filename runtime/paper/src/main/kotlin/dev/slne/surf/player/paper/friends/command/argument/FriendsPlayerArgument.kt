package dev.slne.surf.player.paper.friends.command.argument

import com.github.shynixn.mccoroutine.folia.scope
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.api.core.messages.adventure.uuid
import dev.slne.surf.api.core.messages.adventure.uuidOrNull
import dev.slne.surf.api.paper.command.args.SuspendCustomArgument
import dev.slne.surf.player.api.SurfCommunityPlayer
import dev.slne.surf.player.paper.plugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import net.kyori.adventure.text.Component
import java.util.*

/**
 * Resolves a player related to the command sender (a friend, a request sender, ...).
 *
 * @param find looks up the related player by the sender's uuid and the typed input
 * @param suggestedNames names of all related players of the sender
 * @param notFoundMessage error shown when [find] returns `null`
 */
class FriendsPlayerArgument(
    nodeName: String,
    private val find: suspend (sender: UUID, input: String) -> SurfCommunityPlayer?,
    suggestedNames: suspend (sender: UUID) -> List<String>,
    private val notFoundMessage: Component,
) : SuspendCustomArgument<SurfCommunityPlayer, String>(StringArgument(nodeName)) {

    init {
        replaceSuggestions(
            ArgumentSuggestions.stringsAsync { info ->
                plugin.scope.future {
                    val uuid = info.sender.uuidOrNull() ?: return@future emptyArray()

                    suggestedNames(uuid).toTypedArray()
                }
            }
        )
    }

    override suspend fun CoroutineScope.parse(info: CustomArgumentInfo<String>): SurfCommunityPlayer {
        return find(info.sender.uuid(), info.currentInput)
            ?: throw CustomArgumentException.fromAdventureComponent(notFoundMessage)
    }
}
