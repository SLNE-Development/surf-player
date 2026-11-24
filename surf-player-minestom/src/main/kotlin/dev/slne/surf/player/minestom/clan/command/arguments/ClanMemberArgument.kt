package dev.slne.surf.player.minestom.clan.command.arguments

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.player.core.client.clan.command.clanMemberNames
import dev.slne.surf.player.core.client.clan.command.findClanMember
import dev.slne.surf.player.minestom.command.PlayerLookupArgument

/**
 * Adds an argument naming a clan member by name or uuid; suggests the members of the executor's
 * clan.
 */
fun CommandAPICommand.clanMemberArgument(nodeName: String): CommandAPICommand =
    withArguments(
        PlayerLookupArgument(
            nodeName,
            lookup = { _, nameOrUuid -> findClanMember(nameOrUuid) },
            suggestions = ::clanMemberNames
        )
    )
