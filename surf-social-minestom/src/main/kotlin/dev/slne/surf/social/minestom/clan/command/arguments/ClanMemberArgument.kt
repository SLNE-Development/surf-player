package dev.slne.surf.social.minestom.clan.command.arguments

import dev.slne.surf.social.api.clan.member.ClanMember
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.argument.Argument
import dev.slne.surf.api.minestom.command.argument.CustomArgument
import dev.slne.surf.api.minestom.command.argument.StringArgument
import dev.slne.surf.api.minestom.command.suggestion.ArgumentSuggestions
import dev.slne.surf.social.core.client.clan.command.clanMemberNames
import dev.slne.surf.social.core.client.clan.command.findClanMember
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.minestom.server.entity.Player

class ClanMemberArgument(nodeName: String) :
    CustomArgument<Deferred<ClanMember?>, String>(StringArgument(nodeName), { info ->
        val nameOrUuid = info.currentInput

        clanArgumentScope.async { findClanMember(nameOrUuid) }
    }) {

    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync { info ->
            val player = info.sender as? Player ?: return@stringCollectionAsync emptyList()

            clanMemberNames(player.uuid)
        })
    }
}

inline fun CommandAPICommand.clanMemberArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<Deferred<ClanMember?>>.() -> Unit = {}
): CommandAPICommand =
    withArguments(ClanMemberArgument(nodeName).setOptional(optional).apply(block))
