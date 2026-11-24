package dev.slne.surf.player.core.client.clan.command

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.pagination.Pagination
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.clan.ClanCreationResult
import dev.slne.surf.player.api.clan.clan.ClanTagColorUpdate
import dev.slne.surf.player.api.clan.invite.ClanInvite
import dev.slne.surf.player.api.clan.invite.ClanInviteAcceptResult
import dev.slne.surf.player.api.clan.invite.ClanInviteResult
import dev.slne.surf.player.api.clan.member.ClanMember
import dev.slne.surf.player.api.clan.permission.ClanPermission
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.components.ClanComponents
import dev.slne.surf.player.core.client.clan.redis.ClanCaches
import dev.slne.surf.player.core.client.clan.services.ClientClanService
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.command.CommandOutcome
import dev.slne.surf.player.core.client.command.failure
import dev.slne.surf.player.core.client.command.reply
import dev.slne.surf.player.core.client.util.usernameOrUuid
import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import dev.slne.surf.player.core.common.clan.config.ClanConfig
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextDecoration
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * The clan commands every platform runs: the platforms only parse the arguments, provide click
 * callbacks for confirmations and hand the outcome back to the sender.
 */
object ClanCommands {

    private suspend fun clanOf(player: UUID): Clan? = SurfClanApi.findClanByPlayer(player)

    suspend fun create(player: UUID, name: String, tag: String): CommandOutcome {
        val result = SurfClanApi.createClan(player, name, tag)
        if (result !is ClanCreationResult.Success) {
            return reply(clanCreationFailedMessage(result))
        }

        return reply(clanCreatedMessage(result.clan as ClanImpl))
    }

    /** Shows [clan], or the clan of [player] when no clan is given. */
    suspend fun info(player: UUID?, clan: Clan?): CommandOutcome {
        val shown = clan ?: player?.let { clanOf(it) } ?: return failure(Messages.NOT_IN_CLAN)
        return reply(ClanComponents.renderClanInformation(shown as ClanImpl))
    }

    suspend fun whois(targetUuid: UUID, targetName: String): CommandOutcome =
        reply(whoisMessage(targetName, clanOf(targetUuid)))

    /** Lists the members of [clan], or of the clan of [player] when no clan is given. */
    suspend fun members(player: UUID?, clan: Clan?): CommandOutcome {
        val shown = clan ?: player?.let { clanOf(it) } ?: return failure(Messages.NOT_IN_CLAN)
        val data = collectClanMemberData(shown)

        return reply(clanMembersPagination(shown as ClanImpl).renderComponent(data))
    }

    private val listPagination = Pagination<Clan> {
        title { primary("Clans".toSmallCaps(), TextDecoration.BOLD) }

        rowRenderer { clan, _ ->
            listOf(buildText {
                spacer(">")
                appendSpace()
                variableValue(clan.name)
                appendSpace()
                info("(${clan.tag})")
                hoverEvent(buildText { info("Klicke, um Informationen über den Clan anzuzeigen.") })
                clickEvent(ClickEvent.runCommand("/clan info ${clan.tag}"))
            })
        }
    }

    suspend fun list(page: Int): CommandOutcome {
        val clans = ClientClanService.fetchAllClansWithoutMembersSortByMemberCount()
        return reply(listPagination.renderComponent(clans, page))
    }

    // region invitations

    suspend fun invite(player: UUID, playerName: String, invitee: UUID, inviteeName: String): CommandOutcome {
        val clan = clanOf(player) ?: return failure(Messages.NOT_IN_CLAN)

        if (!clan.hasMemberPermission(player, ClanPermission.INVITE)) return failure(NO_INVITE_PERMISSION)
        if (invitee == player) return failure(CommandMessages.NOT_YOURSELF)

        return when (clan.invite(invitee, player)) {
            ClanInviteResult.AlreadyInClan -> failure(INVITEE_ALREADY_IN_CLAN)
            ClanInviteResult.AlreadyInvited -> failure(INVITEE_ALREADY_INVITED)
            ClanInviteResult.InvitationsDisabled -> failure(INVITEE_DISABLED_INVITATIONS)
            is ClanInviteResult.Success -> {
                SurfCoreApi.getPlayer(invitee)?.let { online ->
                    SurfCoreApi.sendText(online, inviteReceivedMessage(playerName, clan.name, clan.tag))
                }

                reply(inviteSentMessage(inviteeName, clan as ClanImpl))
            }
        }
    }

    suspend fun revoke(player: UUID, invite: ClanInvite): CommandOutcome {
        val clan = clanOf(player) ?: return failure(Messages.NOT_IN_CLAN)
        if (!clan.hasMemberPermission(player, ClanPermission.INVITE)) return failure(NO_INVITE_PERMISSION)

        if (!invite.revoke()) return failure(INVITE_REVOKE_FAILED)

        return reply(inviteRevokedMessage(usernameOrUuid(invite.invited)))
    }

    suspend fun accept(playerName: String, invite: ClanInvite): CommandOutcome =
        when (val result = invite.accept()) {
            ClanInviteAcceptResult.AlreadyInClan -> failure(Messages.ALREADY_IN_CLAN)
            ClanInviteAcceptResult.InviteNotFound -> failure(Messages.INVITE_NOT_FOUND)
            is ClanInviteAcceptResult.Accepted -> {
                val clan = result.clan as ClanImpl
                clan.broadcast(memberJoinedMessage(playerName))
                reply(inviteAcceptedMessage(clan))
            }
        }

    suspend fun deny(playerName: String, invite: ClanInvite): CommandOutcome {
        if (!invite.revoke()) return failure(INVITE_ALREADY_DENIED)

        SurfCoreApi.getPlayer(invite.invitedBy)?.let { inviter ->
            SurfCoreApi.sendText(inviter, inviteDeniedNotification(playerName))
        }

        return reply(inviteDeniedMessage())
    }

    // endregion

    // region members

    suspend fun kick(player: UUID, playerName: String, member: ClanMember): CommandOutcome {
        val clan = clanOf(player) ?: return failure(Messages.NOT_IN_CLAN)

        if (player == member.uuid) return failure(CANNOT_KICK_SELF)
        if (!clan.isMember(member.uuid)) return failure(Messages.PLAYER_NOT_IN_YOUR_CLAN)
        if (!clan.hasMemberPermission(player, ClanPermission.KICK)) return failure(NO_KICK_PERMISSION)

        val executor = clan.getMember(player) ?: return failure(Messages.NOT_IN_CLAN)
        if (member.role >= executor.role) return failure(CANNOT_KICK_SAME_OR_HIGHER_ROLE)

        if (!clan.removeMember(member)) return failure(KICK_FAILED)

        clan.broadcast(memberKickedMessage(usernameOrUuid(member.uuid), playerName))
        return CommandOutcome.Silent
    }

    /** Promotes or demotes [member] by one role on behalf of [player] and announces it to the clan. */
    suspend fun changeRole(player: UUID, playerName: String, member: ClanMember, change: RoleChange): CommandOutcome {
        val clan = clanOf(player) ?: return failure(Messages.NOT_IN_CLAN)

        if (!clan.isMember(member.uuid)) return failure(Messages.PLAYER_NOT_IN_YOUR_CLAN)
        if (member.uuid == player) return failure(change.pick(CANNOT_PROMOTE_SELF, CANNOT_DEMOTE_SELF))

        val permission = change.pick(ClanPermission.PROMOTE, ClanPermission.DEMOTE)
        if (!clan.hasMemberPermission(player, permission)) {
            return failure(change.pick(NO_PROMOTE_PERMISSION, NO_DEMOTE_PERMISSION))
        }

        val executor = clan.getMember(player) ?: return failure(Messages.NOT_IN_CLAN)
        if (member.role >= executor.role) {
            return failure(change.pick(CANNOT_PROMOTE_SAME_OR_HIGHER_ROLE, CANNOT_DEMOTE_SAME_OR_HIGHER_ROLE))
        }

        val oldRole = member.role
        if (!change.pick(oldRole.hasNextRole(), oldRole.hasPreviousRole())) {
            return failure(change.pick(ALREADY_HIGHEST_ROLE, ALREADY_LOWEST_ROLE))
        }

        val newRole = change.pick(oldRole.nextRole(), oldRole.previousRole())
        if (!member.changeRole(newRole)) return failure(ROLE_CHANGE_FAILED)

        clan.broadcast(memberRoleChangedMessage(usernameOrUuid(member.uuid), playerName, oldRole, newRole, change))
        return CommandOutcome.Silent
    }

    private fun <T> RoleChange.pick(promoted: T, demoted: T): T = when (this) {
        RoleChange.PROMOTED -> promoted
        RoleChange.DEMOTED -> demoted
    }

    /**
     * Asks [player] to confirm leaving their clan; [click] creates the confirming click event for
     * the clan's uuid.
     */
    suspend fun leave(player: UUID, click: (clanUuid: UUID) -> ClickEvent<*>): CommandOutcome {
        val clan = clanOf(player) ?: return failure(Messages.NOT_IN_CLAN)
        if (clan.isOwnedBy(player)) return failure(CLAN_OWNER_CANNOT_LEAVE)

        return reply(leaveConfirmationMessage(clan, click(clan.uuid)))
    }

    /**
     * Asks [player] to confirm deleting their clan; [click] creates the confirming click event for
     * the clan's uuid.
     */
    suspend fun delete(player: UUID, click: (clanUuid: UUID) -> ClickEvent<*>): CommandOutcome {
        val clan = clanOf(player) ?: return failure(Messages.NOT_IN_CLAN)
        clan.canBeDisbandedBy(player)?.let { return failure(it.message) }

        return reply(disbandConfirmationMessage(clan, click(clan.uuid)))
    }

    // endregion

    // region options

    suspend fun discord(player: UUID, rawLink: String): CommandOutcome {
        val clan = clanOf(player) ?: return failure(Messages.NOT_IN_CLAN)
        if (!clan.hasMemberPermission(player, ClanPermission.DISCORD)) return failure(NO_DISCORD_PERMISSION)

        val link = rawLink.trim()
        val remove = isRemoveDiscordLink(link)

        if (remove) {
            clan.setDiscordInvite(null)
        } else {
            if (clan.activeMemberCount < Clan.DISCORD_LINK_REQUIRED_MEMBERS) {
                return failure(NOT_ENOUGH_MEMBERS_FOR_DISCORD_LINK)
            }
            if (!isValidDiscordInvite(link)) return failure(INVALID_DISCORD_LINK)

            clan.setDiscordInvite(link)
        }

        return reply(discordLinkChangedMessage(remove, link))
    }

    suspend fun changeColor(player: UUID, update: ClanTagColorUpdate): CommandOutcome {
        val clan = clanOf(player) ?: return failure(Messages.NOT_IN_CLAN)
        if (!clan.hasMemberPermission(player, ClanPermission.OPTIONS_TAG_COLOR)) {
            return failure(NO_TAG_COLOR_PERMISSION)
        }

        clan.changeClanTagColor(update)
        return reply(clanTagColorChangedMessage())
    }

    // endregion

    // region admin

    fun reload(): CommandOutcome {
        val millis = measureTimeMillis { ClanConfig.reloadFromFile() }
        return reply(CommandMessages.configReloaded("clan", millis))
    }

    suspend fun invalidateCaches(): CommandOutcome {
        ClanCaches.invalidateAll()
        return reply(invalidatedCachesMessage())
    }

    suspend fun adminDelete(clan: Clan): CommandOutcome {
        val impl = clan as ClanImpl
        return reply(clanDeletedMessage(impl, impl.delete()))
    }

    // endregion
}
