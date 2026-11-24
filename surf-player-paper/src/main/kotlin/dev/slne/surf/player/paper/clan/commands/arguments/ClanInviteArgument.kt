package dev.slne.surf.social.paper.clan.commands.arguments

import com.github.shynixn.mccoroutine.folia.scope
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.social.api.clan.invite.ClanInvite
import dev.slne.surf.social.paper.clan.commands.failCommand
import dev.slne.surf.social.paper.plugin
import dev.slne.surf.api.paper.command.args.SuspendCustomArgument
import dev.slne.surf.social.core.client.clan.Messages
import dev.slne.surf.social.core.client.clan.command.CLAN_INVITE_ARGUMENT_NEEDS_PLAYER
import dev.slne.surf.social.core.client.clan.command.invitedClanNames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class ClanInviteArgument(nodeName: String) :
    SuspendCustomArgument<ClanInvite, String>(StringArgument(nodeName)) {
    companion object {
        private val suggestions = ArgumentSuggestions<CommandSender> { info, builder ->
            val player = info.sender as? Player ?: return@ArgumentSuggestions builder.buildFuture()
            plugin.scope.future {
                for (clanName in invitedClanNames(player.uniqueId)) {
                    builder.suggest(clanName)
                }

                builder.build()
            }
        }
    }


    init {
        replaceSuggestions(suggestions)
    }

    override suspend fun CoroutineScope.parse(info: CustomArgumentInfo<String>): ClanInvite {
        val clanName = info.currentInput
        val sender = info.sender as? Player
            ?: failCommand(CLAN_INVITE_ARGUMENT_NEEDS_PLAYER)
        val invite = ClanInvite.pendingInviteByPlayerAndClanName(sender.uniqueId, clanName)

        return invite ?: failCommand(Messages.noPendingInviteForClan(clanName))
    }
}
