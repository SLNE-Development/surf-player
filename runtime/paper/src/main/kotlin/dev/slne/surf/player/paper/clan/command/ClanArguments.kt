package dev.slne.surf.player.paper.clan.command

import com.github.shynixn.mccoroutine.folia.scope
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.api.paper.command.args.SuspendCustomArgument
import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.invite.ClanInvite
import dev.slne.surf.player.api.clan.member.ClanMember
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.command.*
import dev.slne.surf.player.core.client.clan.services.ClientClanService
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.paper.command.argumentError
import dev.slne.surf.player.paper.command.failCommand
import dev.slne.surf.player.paper.plugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

/**
 * Suggests what [names] returns for the player running the command, nothing for other senders.
 */
private fun playerSuggestions(names: suspend (UUID) -> Collection<String>) =
    ArgumentSuggestions.stringCollectionAsync<CommandSender> { info ->
        plugin.scope.future {
            val player = info.sender as? Player ?: return@future emptyList()
            names(player.uniqueId)
        }
    }

private fun CustomArgument.CustomArgumentInfo<String>.playerUuid(): UUID =
    (sender as? Player)?.uniqueId ?: failCommand(CommandMessages.PLAYERS_ONLY)

/** Any clan, given by its tag. */
class ClanByTagArgument(nodeName: String) : SuspendCustomArgument<Clan, String>(StringArgument(nodeName)) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync { info ->
            plugin.scope.future { ClientClanService.computeTagSuggestions(info.currentArg) }
        })
    }

    override suspend fun CoroutineScope.parse(info: CustomArgumentInfo<String>): Clan =
        SurfClanApi.findClanByTag(info.currentInput) ?: failCommand(Messages.unknownClanWithTag(info.currentInput))
}

/**
 * The pending invitation of the sender to a clan, given by the clan's tag (or name). With
 * [rejectClanMembers] (`/clan accept`) a sender who already is in a clan is told so, whether or
 * not an invitation exists.
 */
class ClanInviteArgument(
    nodeName: String,
    private val rejectClanMembers: Boolean = false,
) : SuspendCustomArgument<ClanInvite, String>(StringArgument(nodeName)) {
    init {
        replaceSuggestions(playerSuggestions(::invitedClanTags))
    }

    override suspend fun CoroutineScope.parse(info: CustomArgumentInfo<String>): ClanInvite {
        val player = info.playerUuid()
        if (rejectClanMembers && SurfClanApi.findClanByPlayer(player) != null) failCommand(Messages.ALREADY_IN_CLAN)

        return findClanInviteByTagOrName(player, info.currentInput)
            ?: failCommand(Messages.noPendingInviteForClan(info.currentInput))
    }
}

/** A member of the sender's clan, given by name. */
class ClanMemberArgument(nodeName: String) : SuspendCustomArgument<ClanMember, String>(StringArgument(nodeName)) {
    init {
        replaceSuggestions(playerSuggestions(::clanMemberNames))
    }

    override suspend fun CoroutineScope.parse(info: CustomArgumentInfo<String>): ClanMember {
        val player = info.playerUuid()
        SurfClanApi.findClanByPlayer(player) ?: failCommand(Messages.NOT_IN_CLAN)

        return findClanMember(player, info.currentInput) ?: failCommand(Messages.unknownClanMember(info.currentInput))
    }
}

/** A player the sender's clan has invited, given by name. */
class PendingClanInviteArgument(nodeName: String) : SuspendCustomArgument<ClanInvite, String>(StringArgument(nodeName)) {
    init {
        replaceSuggestions(playerSuggestions(::pendingInviteeNames))
    }

    override suspend fun CoroutineScope.parse(info: CustomArgumentInfo<String>): ClanInvite {
        val player = info.playerUuid()
        SurfClanApi.findClanByPlayer(player) ?: failCommand(Messages.NOT_IN_CLAN)

        return findPendingInvite(player, info.currentInput)
            ?: failCommand(Messages.noPendingInviteForPlayer(info.currentInput))
    }
}

/**
 * A hex color of [length] digits, with or without a leading `#`. Greedy, since a `#` cannot be
 * typed into a plain string argument.
 */
abstract class HexColorArgument<T>(
    nodeName: String,
    length: Int,
    suggestions: Collection<String>,
    factory: (String) -> T?,
) : CustomArgument<T, String>(GreedyStringArgument(nodeName), { info ->
    val input = info.currentInput().trim()
    normalizeHexColor(input, length)?.let(factory) ?: throw argumentError(invalidHexColorMessage(input, length))
}) {
    init {
        replaceSuggestions(ArgumentSuggestions.strings(suggestions))
    }
}

class ClanTagColorArgument(nodeName: String) : HexColorArgument<TextColor>(
    nodeName, CLAN_TAG_COLOR_LENGTH, CLAN_TAG_COLOR_SUGGESTIONS, { TextColor.fromHexString("#$it") }
)

class ClanTagShadowColorArgument(nodeName: String) : HexColorArgument<ShadowColor>(
    nodeName, CLAN_TAG_SHADOW_COLOR_LENGTH, CLAN_TAG_SHADOW_COLOR_SUGGESTIONS, { ShadowColor.fromHexString("#$it") }
)
