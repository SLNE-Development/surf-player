package dev.slne.surf.player.core.client.clan

import dev.slne.surf.player.core.client.command.CommandMessages

/**
 * The plain player facing texts a clan command reports through its platform's own failure
 * mechanism, shared by every platform.
 */
object Messages {
    const val NOT_IN_CLAN = "Du bist in keinem Clan."
    const val NO_LONGER_IN_A_CLAN = "Du bist nicht mehr in einem Clan."
    const val ALREADY_IN_CLAN = "Du bist bereits in einem Clan."

    /** For [dev.slne.surf.player.api.clan.invite.ClanInviteAcceptResult.InviteNotFound]. */
    const val INVITE_NOT_FOUND = "Diese Einladung existiert nicht mehr."
    const val PLAYER_NOT_FOUND = CommandMessages.PLAYER_NOT_FOUND
    const val PLAYER_NOT_IN_YOUR_CLAN = "Der Spieler ist nicht in deinem Clan."

    fun unknownClanWithTag(tag: String) = "Es gibt keinen Clan mit dem Tag '$tag'."

    fun unknownClanMember(nameOrUuid: String) = "'$nameOrUuid' ist kein Mitglied deines Clans."

    fun noPendingInviteForClan(clan: String) = "Du hast keine offene Einladung vom Clan '$clan'."

    fun noPendingInviteForPlayer(player: String) = "'$player' hat keine offene Einladung in deinen Clan."
}
