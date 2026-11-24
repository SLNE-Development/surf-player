package dev.slne.surf.player.minestom.clan.command

import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.invite.ClanInvite
import dev.slne.surf.player.api.clan.member.ClanMember
import dev.slne.surf.api.minestom.command.executor.CommandArguments
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.minestom.command.failCommand
import kotlinx.coroutines.Deferred
import java.util.*

/**
 * The clan the [node] argument names, or [player]'s own clan when the argument was left out.
 */
suspend fun CommandArguments.resolveClanArgumentOrOwn(node: String, player: UUID): Clan {
    if (node in this) {
        return resolveClanArgument(node)
    }

    return Clan.byPlayer(player) ?: failCommand(Messages.NOT_IN_CLAN)
}

/**
 * The clan the [node] argument names.
 */
suspend fun CommandArguments.resolveClanArgument(node: String): Clan {
    val tag = getRaw(node).orEmpty()

    return get<Deferred<Clan?>>(node).await()
        ?: failCommand(Messages.unknownClanWithTag(tag))
}

/**
 * The clan member the [node] argument names.
 */
suspend fun CommandArguments.resolveClanMember(node: String): ClanMember =
    get<Deferred<ClanMember?>>(node).await()
        ?: failCommand(Messages.unknownClanMember(getRaw(node).orEmpty()))

/**
 * The pending clan invitation the [node] argument names.
 */
suspend fun CommandArguments.resolveClanInvite(node: String): ClanInvite =
    get<Deferred<ClanInvite?>>(node).await()
        ?: failCommand(Messages.noPendingInviteForClan(getRaw(node).orEmpty()))
