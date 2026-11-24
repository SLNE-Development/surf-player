package dev.slne.surf.player.paper.clan.commands.arguments

import com.github.shynixn.mccoroutine.folia.scope
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.paper.clan.commands.failCommand
import dev.slne.surf.player.paper.plugin
import dev.slne.surf.api.paper.command.args.SuspendCustomArgument
import dev.slne.surf.player.core.client.clan.services.ClientClanService
import dev.slne.surf.player.core.client.clan.Messages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import org.bukkit.command.CommandSender

class ClanByClanTagArgument(nodeName: String) :
    SuspendCustomArgument<Clan, String>(StringArgument(nodeName)) {
    companion object {
        private val suggestions = ArgumentSuggestions<CommandSender> { info, builder ->
            plugin.scope.future {
                for (suggestion in ClientClanService.computeTagSuggestions(info.currentArg)) {
                    builder.suggest(suggestion)
                }
                builder.build()
            }
        }
    }

    init {
        replaceSuggestions(suggestions)
    }

    override suspend fun CoroutineScope.parse(info: CustomArgumentInfo<String>): Clan {
        val tag = info.currentInput
        val clan = SurfClanApi.findClanByTag(tag)
            ?: failCommand(Messages.unknownClanWithTag(tag))
        return clan
    }
}
