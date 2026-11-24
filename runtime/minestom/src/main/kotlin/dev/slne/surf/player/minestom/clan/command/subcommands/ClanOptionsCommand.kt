package dev.slne.surf.player.minestom.clan.command.subcommands

import dev.slne.surf.player.minestom.clan.command.subcommands.options.clanTagColorCommand
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.player.core.client.clan.permission.ClanPermissions

fun CommandAPICommand.clanOptionsCommand(): CommandAPICommand = withSubcommand(
    subcommand("options") {
        withPermission(ClanPermissions.CLAN_OPTIONS_COMMAND)

        clanTagColorCommand()
    }
)
