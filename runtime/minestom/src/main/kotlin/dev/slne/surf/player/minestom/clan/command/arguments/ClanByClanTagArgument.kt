package dev.slne.surf.player.minestom.clan.command.arguments

import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.argument.Argument
import dev.slne.surf.api.minestom.command.argument.CustomArgument
import dev.slne.surf.api.minestom.command.argument.StringArgument
import dev.slne.surf.api.minestom.command.suggestion.ArgumentSuggestions
import dev.slne.surf.player.core.common.clan.clan.CoreClanService
import dev.slne.surf.player.minestom.command.commandArgumentScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

class ClanByClanTagArgument(nodeName: String) :
    CustomArgument<Deferred<Clan?>, String>(StringArgument(nodeName), { info ->
        val tag = info.currentInput
        commandArgumentScope.async { Clan.byTag(tag) }
    }) {

    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync { info ->
            CoreClanService.computeTagSuggestions(info.currentArg)
        })
    }
}

inline fun CommandAPICommand.clanByClanTagArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<Deferred<Clan?>>.() -> Unit = {}
): CommandAPICommand =
    withArguments(ClanByClanTagArgument(nodeName).setOptional(optional).apply(block))
