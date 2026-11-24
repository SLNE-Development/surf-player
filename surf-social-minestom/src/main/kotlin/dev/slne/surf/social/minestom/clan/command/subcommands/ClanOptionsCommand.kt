package dev.slne.surf.social.minestom.clan.command.subcommands

import dev.slne.surf.social.minestom.clan.command.subcommands.options.clanTagColorCommand
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.social.core.client.clan.permission.ClanPermissions

fun CommandAPICommand.clanOptionsCommand(): CommandAPICommand = withSubcommand(
    subcommand("options") {
        withPermission(ClanPermissions.CLAN_OPTIONS_COMMAND)

        clanTagColorCommand()
    }
)
