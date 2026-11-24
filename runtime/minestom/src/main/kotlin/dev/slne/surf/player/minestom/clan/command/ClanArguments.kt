package dev.slne.surf.player.minestom.clan.command

import dev.slne.surf.api.minestom.command.argument.CustomArgument
import dev.slne.surf.api.minestom.command.argument.GreedyStringArgument
import dev.slne.surf.api.minestom.command.argument.StringArgument
import dev.slne.surf.api.minestom.command.executor.CommandArguments
import dev.slne.surf.api.minestom.command.suggestion.ArgumentSuggestions
import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.invite.ClanInvite
import dev.slne.surf.player.api.clan.member.ClanMember
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.command.*
import dev.slne.surf.player.core.client.clan.services.ClientClanService
import dev.slne.surf.player.minestom.command.PlayerLookupArgument
import dev.slne.surf.player.minestom.command.commandArgumentScope
import dev.slne.surf.player.minestom.command.failCommand
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import java.util.*

/** Any clan, given by its tag; resolves to `null` for an unknown tag. */
class ClanByTagArgument(nodeName: String) : CustomArgument<Deferred<Clan?>, String>(StringArgument(nodeName), { info ->
    val tag = info.currentInput
    commandArgumentScope.async { SurfClanApi.findClanByTag(tag) }
}) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync { info ->
            ClientClanService.computeTagSuggestions(info.currentArg)
        })
    }
}

/** The pending invitation of the sender to a clan, given by the clan's tag (or name). */
fun clanInviteArgument(nodeName: String) =
    PlayerLookupArgument(nodeName, ::findClanInviteByTagOrName) { player -> invitedClanTags(player) }

/** A member of the sender's clan, given by name. */
fun clanMemberArgument(nodeName: String) =
    PlayerLookupArgument(nodeName, ::findClanMember) { player -> clanMemberNames(player) }

/** A player the sender's clan has invited, given by name. */
fun pendingClanInviteArgument(nodeName: String) =
    PlayerLookupArgument(nodeName, ::findPendingInvite) { player -> pendingInviteeNames(player) }

/** The clan the [node] argument names, `null` when it was left out. */
suspend fun CommandArguments.optionalClan(node: String): Clan? {
    if (node !in this) return null
    return get<Deferred<Clan?>>(node).await() ?: failCommand(Messages.unknownClanWithTag(getRaw(node).orEmpty()))
}

suspend fun CommandArguments.clan(node: String): Clan =
    optionalClan(node) ?: failCommand(Messages.unknownClanWithTag(getRaw(node).orEmpty()))

suspend fun CommandArguments.clanInvite(node: String): ClanInvite =
    get<Deferred<ClanInvite?>>(node).await() ?: failCommand(Messages.noPendingInviteForClan(getRaw(node).orEmpty()))

suspend fun CommandArguments.clanMember(node: String, player: UUID): ClanMember {
    SurfClanApi.findClanByPlayer(player) ?: failCommand(Messages.NOT_IN_CLAN)
    return get<Deferred<ClanMember?>>(node).await() ?: failCommand(Messages.unknownClanMember(getRaw(node).orEmpty()))
}

suspend fun CommandArguments.pendingInvite(node: String, player: UUID): ClanInvite {
    SurfClanApi.findClanByPlayer(player) ?: failCommand(Messages.NOT_IN_CLAN)
    return get<Deferred<ClanInvite?>>(node).await()
        ?: failCommand(Messages.noPendingInviteForPlayer(getRaw(node).orEmpty()))
}

/**
 * A hex color of [length] digits, with or without a leading `#`. Greedy, since a `#` cannot be
 * typed into a plain string argument.
 */
abstract class HexColorArgument<T>(
    nodeName: String,
    length: Int,
    suggestions: List<String>,
    factory: (String) -> T?,
) : CustomArgument<T, String>(GreedyStringArgument(nodeName), { info ->
    val input = info.currentInput.trim()
    normalizeHexColor(input, length)?.let(factory) ?: failCommand(invalidHexColorMessage(input, length))
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
