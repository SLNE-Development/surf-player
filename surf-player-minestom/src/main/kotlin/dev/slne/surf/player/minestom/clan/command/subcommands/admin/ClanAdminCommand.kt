package dev.slne.surf.social.minestom.clan.command.subcommands.admin

import dev.slne.surf.social.minestom.clan.command.arguments.clanByClanTagArgument
import dev.slne.surf.social.minestom.clan.command.resolveClanArgument
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.social.core.common.clan.clan.ClanImpl
import dev.slne.surf.social.core.client.clan.command.clanConfigReloadedMessage
import dev.slne.surf.social.core.client.clan.command.clanDeletedMessage
import dev.slne.surf.social.core.client.clan.command.invalidatedCachesMessage
import dev.slne.surf.social.core.client.clan.command.invalidatingCachesMessage
import dev.slne.surf.social.core.client.clan.permission.ClanPermissions
import dev.slne.surf.social.core.client.clan.redis.ClanCaches
import dev.slne.surf.social.core.common.clan.config.ClanConfig

fun CommandAPICommand.clanAdminCommand(): CommandAPICommand = withSubcommand(
    subcommand("admin") {
        withPermission(ClanPermissions.CLAN_ADMIN_COMMAND)

        clanReloadCommand()
        clanInvalidateAllCachesCommand()
        clanDeleteCommand()
    }
)

private fun CommandAPICommand.clanReloadCommand(): CommandAPICommand = withSubcommand(
    subcommand("reload") {
        withPermission(ClanPermissions.CLAN_ADMIN_RELOAD_COMMAND)

        anyExecutor { source, _ ->
            ClanConfig.reloadFromFile()

            source.sendMessage(clanConfigReloadedMessage())
        }
    }
)

private fun CommandAPICommand.clanInvalidateAllCachesCommand(): CommandAPICommand = withSubcommand(
    subcommand("invalidateAllCaches") {
        withPermission(ClanPermissions.CLAN_ADMIN_INVALIDATE_CACHE_COMMAND)

        anyExecutorSuspend { source, _ ->
            source.sendMessage(invalidatingCachesMessage())

            ClanCaches.invalidateAll()

            source.sendMessage(invalidatedCachesMessage())
        }
    }
)

private fun CommandAPICommand.clanDeleteCommand(): CommandAPICommand = withSubcommand(
    subcommand("delete") {
        withPermission(ClanPermissions.CLAN_ADMIN_DELETE_COMMAND)
        clanByClanTagArgument("clan")

        anyExecutorSuspend { sender, arguments ->
            val clan = arguments.resolveClanArgument("clan") as ClanImpl
            val deleted = clan.delete()
            sender.sendMessage(clanDeletedMessage(clan, deleted))
        }
    }
)
