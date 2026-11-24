package dev.slne.surf.player.paper.clan.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.core.command.args.awaitingOrNull
import dev.slne.surf.api.paper.command.args.asyncSignedMessageArgument
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.paper.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.player.api.chat.SurfChatApi
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.api.chat.message.MessageType
import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.clan.ClanTagColor
import dev.slne.surf.player.api.clan.clan.ClanTagColorUpdate
import dev.slne.surf.player.api.clan.invite.ClanInvite
import dev.slne.surf.player.api.clan.member.ClanMember
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
import dev.slne.surf.player.paper.command.CONFIRMATION_LIFETIME
import dev.slne.surf.player.paper.command.PaperAdminAction
import dev.slne.surf.player.paper.command.failCommand
import dev.slne.surf.player.paper.command.helpExecutor
import dev.slne.surf.player.paper.command.page
import dev.slne.surf.player.paper.command.pageArgument
import dev.slne.surf.player.paper.command.playerClickEvent
import dev.slne.surf.player.paper.command.reply
import dev.slne.surf.player.paper.command.toggleArgument
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import java.time.OffsetDateTime
import java.util.*

private const val PLAYER = "spieler"
private const val MEMBER = "mitglied"
private const val CLAN = "clan"
private const val COLOR = "farbe"
private const val MESSAGE = "nachricht"

/**
 * `/clan` with every subcommand, plus the shortcut `/cc`.
 */
fun registerClanCommands() {
    commandAPICommand("clan") {
        withPermission(ClanPermissions.COMMAND_CLAN)
        helpExecutor(clanCommandHelp)

        subcommand("create") {
            withPermission(ClanPermissions.COMMAND_CREATE)
            stringArgument("name")
            stringArgument("tag")
            playerExecutorSuspend { player, args ->
                val name: String by args
                val tag: String by args
                player.reply(ClanCommands.create(player.uniqueId, name, tag))
            }
        }

        subcommand("info") {
            withPermission(ClanPermissions.COMMAND_INFO)
            withOptionalArguments(ClanByTagArgument(CLAN))
            anyExecutorSuspend { sender, args ->
                val clan = args.awaitingOrNull<Clan>(CLAN)
                sender.reply(ClanCommands.info((sender as? Player)?.uniqueId, clan))
            }
        }

        subcommand("whois") {
            withPermission(ClanPermissions.COMMAND_WHOIS)
            surfOfflinePlayerArgument(PLAYER)
            anyExecutorSuspend { sender, args ->
                val target = args.awaitingOrNull<SurfPlayer?>(PLAYER) ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)
                sender.reply(ClanCommands.whois(target.uuid, target.lastKnownName ?: target.uuid.toString()))
            }
        }

        subcommand("list") {
            withPermission(ClanPermissions.COMMAND_LIST)
            pageArgument()
            anyExecutorSuspend { sender, args -> sender.reply(ClanCommands.list(args.page)) }
        }

        subcommand("members") {
            withPermission(ClanPermissions.COMMAND_MEMBERS)
            withOptionalArguments(ClanByTagArgument(CLAN))
            anyExecutorSuspend { sender, args ->
                val clan = args.awaitingOrNull<Clan>(CLAN)
                sender.reply(ClanCommands.members((sender as? Player)?.uniqueId, clan))
            }
        }

        subcommand(clanChatCommand("chat"))

        subcommand("invite") {
            withPermission(ClanPermissions.COMMAND_INVITE)
            surfOfflinePlayerArgument(PLAYER)
            playerExecutorSuspend { player, args ->
                val invitee = args.awaitingOrNull<SurfPlayer?>(PLAYER) ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)
                val inviteeName = invitee.lastKnownName ?: invitee.uuid.toString()
                player.reply(ClanCommands.invite(player.uniqueId, player.name, invitee.uuid, inviteeName))
            }
        }

        subcommand("revoke") {
            withPermission(ClanPermissions.COMMAND_REVOKE)
            withArguments(PendingClanInviteArgument(PLAYER))
            playerExecutorSuspend { player, args ->
                player.reply(ClanCommands.revoke(player.uniqueId, args.awaiting<ClanInvite>(PLAYER)))
            }
        }

        subcommand("accept") {
            withPermission(ClanPermissions.COMMAND_ACCEPT)
            withArguments(ClanInviteArgument(CLAN))
            playerExecutorSuspend { player, args ->
                player.reply(ClanCommands.accept(player.name, args.awaiting<ClanInvite>(CLAN)))
            }
        }

        subcommand("deny") {
            withPermission(ClanPermissions.COMMAND_DENY)
            withArguments(ClanInviteArgument(CLAN))
            playerExecutorSuspend { player, args ->
                player.reply(ClanCommands.deny(player.name, args.awaiting<ClanInvite>(CLAN)))
            }
        }

        subcommand("promote") {
            withPermission(ClanPermissions.COMMAND_PROMOTE)
            withArguments(ClanMemberArgument(MEMBER))
            playerExecutorSuspend { player, args ->
                val member = args.awaiting<ClanMember>(MEMBER)
                player.reply(ClanCommands.changeRole(player.uniqueId, player.name, member, RoleChange.PROMOTED))
            }
        }

        subcommand("demote") {
            withPermission(ClanPermissions.COMMAND_DEMOTE)
            withArguments(ClanMemberArgument(MEMBER))
            playerExecutorSuspend { player, args ->
                val member = args.awaiting<ClanMember>(MEMBER)
                player.reply(ClanCommands.changeRole(player.uniqueId, player.name, member, RoleChange.DEMOTED))
            }
        }

        subcommand("kick") {
            withPermission(ClanPermissions.COMMAND_KICK)
            withArguments(ClanMemberArgument(MEMBER))
            playerExecutorSuspend { player, args ->
                player.reply(ClanCommands.kick(player.uniqueId, player.name, args.awaiting<ClanMember>(MEMBER)))
            }
        }

        subcommand("discord") {
            withPermission(ClanPermissions.COMMAND_DISCORD)
            greedyStringArgument("link") {
                includeSuggestions(ArgumentSuggestions.strings(DISCORD_LINK_SUGGESTIONS))
            }
            playerExecutorSuspend { player, args ->
                val link: String by args
                player.reply(ClanCommands.discord(player.uniqueId, link))
            }
        }

        subcommand("color") {
            withPermission(ClanPermissions.COMMAND_COLOR)

            subcommand("foreground") {
                withPermission(ClanPermissions.TAG_COLOR_FOREGROUND)
                withArguments(ClanTagColorArgument(COLOR))
                playerExecutorSuspend { player, args ->
                    val farbe: TextColor by args
                    changeColor(player, ClanTagColor.update { foreground(farbe) })
                }
            }

            subcommand("background") {
                withPermission(ClanPermissions.TAG_COLOR_BACKGROUND)
                withArguments(ClanTagColorArgument(COLOR))
                playerExecutorSuspend { player, args ->
                    val farbe: TextColor by args
                    changeColor(player, ClanTagColor.update { background(farbe) })
                }
            }

            subcommand("shadow") {
                withPermission(ClanPermissions.TAG_COLOR_SHADOW)
                withArguments(ClanTagShadowColorArgument(COLOR))
                playerExecutorSuspend { player, args ->
                    val farbe: ShadowColor by args
                    changeColor(player, ClanTagColor.update { shadow(farbe) })
                }
            }
        }

        subcommand("leave") {
            withPermission(ClanPermissions.COMMAND_LEAVE)
            playerExecutorSuspend { player, _ ->
                player.reply(ClanCommands.leave(player.uniqueId) { clanUuid ->
                    playerClickEvent(CONFIRMATION_LIFETIME) { clicked ->
                        handleLeaveClick(clicked, clicked.uniqueId, clicked.name, clanUuid)
                    }
                })
            }
        }

        subcommand("delete") {
            withPermission(ClanPermissions.COMMAND_DELETE)
            playerExecutorSuspend { player, _ ->
                player.reply(ClanCommands.delete(player.uniqueId) { clanUuid ->
                    playerClickEvent(CONFIRMATION_LIFETIME) { clicked ->
                        handleDisbandClick(clicked, clicked.uniqueId, clanUuid)
                    }
                })
            }
        }
    }

    clanChatCommand("cc").register()
}

private suspend fun changeColor(player: Player, update: ClanTagColorUpdate) =
    player.reply(ClanCommands.changeColor(player.uniqueId, update))

/**
 * `/clan chat <nachricht>`, also registered as `/cc <nachricht>`.
 */
private fun clanChatCommand(name: String) = CommandAPICommand(name).apply {
    withPermission(ClanPermissions.COMMAND_CHAT)
    asyncSignedMessageArgument(MESSAGE)

    playerExecutorSuspend { player, args ->
        val clan = SurfClanApi.findClanByPlayer(player.uniqueId) ?: failCommand(Messages.NOT_IN_CLAN)
        val message = args.awaiting<SignedMessage>(MESSAGE)
        val formatted = formatClanChatMessage(message.message(), prefixedName(player.uniqueId, player.name))

        sendClanChatMessage(player.uniqueId, clan, message, formatted)

        val messageData = MessageData(
            message = Component.text(message.message()),
            messageUuid = UUID.randomUUID(),
            sender = player.uniqueId,
            receiver = null,
            sentAt = OffsetDateTime.now(),
            server = SurfServer.current().name,
            signature = message.signature(),
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
    PaperAdminAction("reload", ClanPermissions.ADMIN_RELOAD, "Lädt die Clan-Konfiguration neu") {
        anyExecutor { sender, _ -> sender.reply(ClanCommands.reload()) }
    },
    PaperAdminAction("invalidatecaches", ClanPermissions.ADMIN_INVALIDATE_CACHES, "Leert alle Clan-Caches") {
        anyExecutorSuspend { sender, _ -> sender.reply(ClanCommands.invalidateCaches()) }
    },
    PaperAdminAction("delete", ClanPermissions.ADMIN_DELETE, "Löscht einen Clan", "<clan>") {
        withArguments(ClanByTagArgument(CLAN))
        anyExecutorSuspend { sender, args -> sender.reply(ClanCommands.adminDelete(args.awaiting<Clan>(CLAN))) }
    },
)
