package dev.slne.surf.social.paper.clan.commands.subcommands.admin.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.social.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.social.core.client.clan.command.invalidatedCachesMessage
import dev.slne.surf.social.core.client.clan.command.invalidatingCachesMessage
import dev.slne.surf.social.core.client.clan.redis.RedisService

fun CommandAPICommand.clanInvalidateAllCachesCommand() = subcommand("invalidateAllCaches") {
    withPermission(ClanPermissions.CLAN_ADMIN_INVALIDATE_CACHE_COMMAND)

    anyExecutorSuspend { source, arguments ->
        source.sendMessage(invalidatingCachesMessage())

        RedisService.get().invalidateAllCaches()

        source.sendMessage(invalidatedCachesMessage())
    }
}
