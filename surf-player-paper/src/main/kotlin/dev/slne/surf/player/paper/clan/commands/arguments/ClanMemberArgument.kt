package dev.slne.surf.player.paper.clan.commands.arguments

import com.github.shynixn.mccoroutine.folia.scope
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.player.api.clan.member.ClanMember
import dev.slne.surf.player.paper.clan.commands.failCommand
import dev.slne.surf.player.paper.plugin
import dev.slne.surf.api.paper.command.args.SuspendCustomArgument
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.command.clanMemberNames
import dev.slne.surf.player.core.client.clan.command.findClanMember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class ClanMemberArgument(nodeName: String) :
    SuspendCustomArgument<ClanMember, String>(StringArgument(nodeName)) {
    companion object {
        private val suggestions = ArgumentSuggestions<CommandSender> { info, builder ->
            val player = info.sender as? Player ?: return@ArgumentSuggestions builder.buildFuture()
            plugin.scope.future {
                clanMemberNames(player.uniqueId).forEach { builder.suggest(it) }

                builder.build()
            }
        }
    }

    init {
        replaceSuggestions(suggestions)
    }

    override suspend fun CoroutineScope.parse(info: CustomArgumentInfo<String>): ClanMember {
        val playerNameOrUuid = info.currentInput

        return findClanMember(playerNameOrUuid)
            ?: failCommand(Messages.unknownClanMember(playerNameOrUuid))
    }
}
