package dev.slne.surf.player.minestom.clan.command

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.argument.GreedyStringArgument
import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.commandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.signedMessageArgument
import dev.slne.surf.api.minestom.command.dsl.stringArgument
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.api.minestom.command.suggestion.ArgumentSuggestions
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.player.minestom.command.asViewer
import dev.slne.surf.player.minestom.command.visibleOfflinePlayerArgument
import dev.slne.surf.player.api.chat.SurfChatApi
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.api.chat.message.MessageType
import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.ClanTagColor
import dev.slne.surf.player.api.clan.clan.ClanTagColorUpdate
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.chat.formatClanChatMessage
import dev.slne.surf.player.core.client.clan.chat.sendClanChatMessage
import dev.slne.surf.player.core.client.clan.command.ClanCommands
import dev.slne.surf.player.core.client.clan.command.DISCORD_LINK_SUGGESTIONS
import dev.slne.surf.player.core.client.clan.command.RoleChange
import dev.slne.surf.player.core.client.clan.command.clanCommandHelp
import dev.slne.surf.player.core.client.clan.command.handleDisbandClick
import dev.slne.surf.player.core.client.clan.command.handleLeaveClick
import dev.slne.surf.player.core.client.clan.permission.ClanPermissions
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.util.prefixedName
import dev.slne.surf.player.minestom.command.CONFIRMATION_LIFETIME
import dev.slne.surf.player.minestom.command.MinestomAdminAction
import dev.slne.surf.player.minestom.command.failCommand
import dev.slne.surf.player.minestom.command.helpExecutor
import dev.slne.surf.player.minestom.command.page
import dev.slne.surf.player.minestom.command.pageArgument
import dev.slne.surf.player.minestom.command.playerClickEvent
import dev.slne.surf.player.minestom.command.reply
import kotlinx.coroutines.Deferred
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.entity.Player
import java.time.OffsetDateTime
import java.util.*

private const val PLAYER = "spieler"
private const val MEMBER = "mitglied"
private const val CLAN = "clan"
private const val COLOR = "farbe"

/**
 * `/clan` with every subcommand, plus the shortcut `/cc`.
 */
fun registerClanCommands() {
    commandAPICommand("clan") {
        withPermission(ClanPermissions.COMMAND_CLAN)
        helpExecutor(clanCommandHelp)

        withSubcommand(subcommand("create") {
            withPermission(ClanPermissions.COMMAND_CREATE)
            stringArgument("name")
            stringArgument("tag")
            playerExecutorSuspend { player, args ->
                val name: String by args
                val tag: String by args
                player.reply(ClanCommands.create(player.uuid, name, tag))
            }
        })

        withSubcommand(subcommand("info") {
            withPermission(ClanPermissions.COMMAND_INFO)
            withOptionalArguments(ClanByTagArgument(CLAN))
            anyExecutorSuspend { sender, args ->
                sender.reply(ClanCommands.info((sender as? Player)?.uuid, args.optionalClan(CLAN)))
            }
        })

        withSubcommand(subcommand("whois") {
            withPermission(ClanPermissions.COMMAND_WHOIS)
            visibleOfflinePlayerArgument(PLAYER)
            anyExecutorSuspend { sender, args ->
                val target = args.get<Deferred<SurfPlayer?>>(PLAYER).await()
                    ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)
                sender.reply(ClanCommands.whois(target.uuid, target.lastKnownName ?: target.uuid.toString()))
            }
        })

        withSubcommand(subcommand("list") {
            withPermission(ClanPermissions.COMMAND_LIST)
            pageArgument()
            anyExecutorSuspend { sender, args -> sender.reply(ClanCommands.list(args.page)) }
        })

        withSubcommand(subcommand("members") {
            withPermission(ClanPermissions.COMMAND_MEMBERS)
            withOptionalArguments(ClanByTagArgument(CLAN))
            anyExecutorSuspend { sender, args ->
                sender.reply(ClanCommands.members(sender.asViewer(), args.optionalClan(CLAN)))
            }
        })

        withSubcommand(clanChatCommand("chat"))

        withSubcommand(subcommand("invite") {
            withPermission(ClanPermissions.COMMAND_INVITE)
            visibleOfflinePlayerArgument(PLAYER)
            playerExecutorSuspend { player, args ->
                val invitee = args.get<Deferred<SurfPlayer?>>(PLAYER).await()
                    ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)
                val inviteeName = invitee.lastKnownName ?: invitee.uuid.toString()
                player.reply(ClanCommands.invite(player.uuid, player.username, invitee.uuid, inviteeName))
            }
        })

        withSubcommand(subcommand("revoke") {
            withPermission(ClanPermissions.COMMAND_REVOKE)
            withArguments(pendingClanInviteArgument(PLAYER))
            playerExecutorSuspend { player, args ->
                player.reply(ClanCommands.revoke(player.uuid, args.pendingInvite(PLAYER, player.uuid)))
            }
        })

        withSubcommand(subcommand("accept") {
            withPermission(ClanPermissions.COMMAND_ACCEPT)
            withArguments(clanInviteArgument(CLAN))
            playerExecutorSuspend { player, args ->
                player.reply(ClanCommands.accept(player.username, args.clanInvite(CLAN)))
            }
        })

        withSubcommand(subcommand("deny") {
            withPermission(ClanPermissions.COMMAND_DENY)
            withArguments(clanInviteArgument(CLAN))
            playerExecutorSuspend { player, args ->
                player.reply(ClanCommands.deny(player.username, args.clanInvite(CLAN)))
            }
        })

        withSubcommand(subcommand("promote") {
            withPermission(ClanPermissions.COMMAND_PROMOTE)
            withArguments(clanMemberArgument(MEMBER))
            playerExecutorSuspend { player, args ->
                val member = args.clanMember(MEMBER, player.uuid)
                player.reply(ClanCommands.changeRole(player.uuid, player.username, member, RoleChange.PROMOTED))
            }
        })

        withSubcommand(subcommand("demote") {
            withPermission(ClanPermissions.COMMAND_DEMOTE)
            withArguments(clanMemberArgument(MEMBER))
            playerExecutorSuspend { player, args ->
                val member = args.clanMember(MEMBER, player.uuid)
                player.reply(ClanCommands.changeRole(player.uuid, player.username, member, RoleChange.DEMOTED))
            }
        })

        withSubcommand(subcommand("kick") {
            withPermission(ClanPermissions.COMMAND_KICK)
            withArguments(clanMemberArgument(MEMBER))
            playerExecutorSuspend { player, args ->
                player.reply(ClanCommands.kick(player.uuid, player.username, args.clanMember(MEMBER, player.uuid)))
            }
        })

        withSubcommand(subcommand("discord") {
            withPermission(ClanPermissions.COMMAND_DISCORD)
            withArguments(GreedyStringArgument("link").replaceSuggestions(ArgumentSuggestions.strings(DISCORD_LINK_SUGGESTIONS)))
            playerExecutorSuspend { player, args ->
                val link: String by args
                player.reply(ClanCommands.discord(player.uuid, link))
            }
        })

        withSubcommand(subcommand("color") {
            withPermission(ClanPermissions.COMMAND_COLOR)

            withSubcommand(subcommand("foreground") {
                withPermission(ClanPermissions.TAG_COLOR_FOREGROUND)
                withArguments(ClanTagColorArgument(COLOR))
                playerExecutorSuspend { player, args ->
                    val farbe: TextColor by args
                    changeColor(player, ClanTagColor.update { foreground(farbe) })
                }
            })

            withSubcommand(subcommand("background") {
                withPermission(ClanPermissions.TAG_COLOR_BACKGROUND)
                withArguments(ClanTagColorArgument(COLOR))
                playerExecutorSuspend { player, args ->
                    val farbe: TextColor by args
                    changeColor(player, ClanTagColor.update { background(farbe) })
                }
            })

            withSubcommand(subcommand("shadow") {
                withPermission(ClanPermissions.TAG_COLOR_SHADOW)
                withArguments(ClanTagShadowColorArgument(COLOR))
                playerExecutorSuspend { player, args ->
                    val farbe: ShadowColor by args
                    changeColor(player, ClanTagColor.update { shadow(farbe) })
                }
            })
        })

        withSubcommand(subcommand("leave") {
            withPermission(ClanPermissions.COMMAND_LEAVE)
            playerExecutorSuspend { player, _ ->
                player.reply(ClanCommands.leave(player.uuid) { clanUuid ->
                    playerClickEvent(CONFIRMATION_LIFETIME) { clicked ->
                        handleLeaveClick(clicked, clicked.uuid, clicked.username, clanUuid)
                    }
                })
            }
        })

        withSubcommand(subcommand("delete") {
            withPermission(ClanPermissions.COMMAND_DELETE)
            playerExecutorSuspend { player, _ ->
                player.reply(ClanCommands.delete(player.uuid) { clanUuid ->
                    playerClickEvent(CONFIRMATION_LIFETIME) { clicked ->
                        handleDisbandClick(clicked, clicked.uuid, clanUuid)
                    }
                })
            }
        })
    }

    clanChatCommand("cc").withAliases("clanchat").register()
}

private suspend fun changeColor(player: Player, update: ClanTagColorUpdate) =
    player.reply(ClanCommands.changeColor(player.uuid, update))

/**
 * `/clan chat <nachricht>`, also registered as `/cc <nachricht>` and `/clanchat <nachricht>`.
 */
private fun clanChatCommand(name: String): CommandAPICommand = subcommand(name) {
    withPermission(ClanPermissions.COMMAND_CHAT)
    signedMessageArgument("nachricht")

    playerExecutorSuspend { player, args ->
        val clan = SurfClanApi.findClanByPlayer(player.uuid) ?: failCommand(Messages.NOT_IN_CLAN)
        val nachricht: SignedMessage by args
        val formatted = formatClanChatMessage(nachricht.message(), prefixedName(player.uuid, player.username))

        sendClanChatMessage(player.uuid, clan, nachricht, formatted)

        val messageData = MessageData(
            message = Component.text(nachricht.message()),
            messageUuid = UUID.randomUUID(),
            sender = player.uuid,
            receiver = null,
            sentAt = OffsetDateTime.now(),
            server = SurfServer.current().name,
            signature = nachricht.signature(),
            type = MessageType("CLAN_CHAT")
        )

        SurfChatApi.logMessage(messageData)
        SurfChatApi.passAutoMod(messageData)
    }
}

/**
 * The actions of `/surfplayer clan`.
 */
fun clanAdminActions() = listOf(
    MinestomAdminAction("reload", ClanPermissions.ADMIN_RELOAD, "Lädt die Clan-Konfiguration neu") {
        anyExecutor { sender, _ -> sender.reply(ClanCommands.reload()) }
    },
    MinestomAdminAction("invalidatecaches", ClanPermissions.ADMIN_INVALIDATE_CACHES, "Leert alle Clan-Caches") {
        anyExecutorSuspend { sender, _ -> sender.reply(ClanCommands.invalidateCaches()) }
    },
    MinestomAdminAction("delete", ClanPermissions.ADMIN_DELETE, "Löscht einen Clan", "<clan>") {
        withArguments(ClanByTagArgument(CLAN))
        anyExecutorSuspend { sender, args -> sender.reply(ClanCommands.adminDelete(args.clan(CLAN))) }
    },
)
