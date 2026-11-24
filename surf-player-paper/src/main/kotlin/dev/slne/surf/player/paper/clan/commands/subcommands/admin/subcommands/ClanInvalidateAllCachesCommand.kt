package dev.slne.surf.player.paper.clan.commands.subcommands.admin.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.player.core.client.clan.command.invalidatedCachesMessage
import dev.slne.surf.player.core.client.clan.command.invalidatingCachesMessage
import dev.slne.surf.player.core.client.clan.redis.ClanCaches
import dev.slne.surf.player.paper.clan.permission.ClanPermissions

fun CommandAPICommand.clanInvalidateAllCachesCommand() = subcommand("invalidateAllCaches") {
    withPermission(ClanPermissions.CLAN_ADMIN_INVALIDATE_CACHE_COMMAND)

    anyExecutorSuspend { source, _ ->
        source.sendMessage(invalidatingCachesMessage())

        ClanCaches.invalidateAll()

        source.sendMessage(invalidatedCachesMessage())
    }
}
