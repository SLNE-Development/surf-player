package dev.slne.surf.chat.bukkit.command.channel

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.chat.bukkit.command.argument.channelArgument
import dev.slne.surf.chat.bukkit.permission.SurfChatPermissionRegistry
import dev.slne.surf.surfapi.core.api.util.emptyObjectSet

fun CommandAPICommand.channelSpyCommand() = subcommand("spy") {
    withPermission(SurfChatPermissionRegistry.COMMAND_CHANNEL_ADMIN_SPY)
    channelArgument("channel")
    channelSpyClearCommand()
    playerExecutor { player, args ->
        val channels = args.getOrDefaultUnchecked("channels", emptyObjectSet<ChannelModel>())

        if (channels.isEmpty()) {
            if (!spyService.isChannelSpying(player.uniqueId)) {
                player.sendPrefixed {
                    error("Du spionierst in keinem Kanal.")
                }
                return@playerExecutor
            }

            spyService.clearChannelSpies(player.uniqueId)

            player.sendPrefixed {
                success("Du spionierst in keinem Kanal mehr.")
            }
            return@playerExecutor
        }

        channels.forEach {
            if (!spyService.getChannelSpies(it).contains(player.uniqueId)) {
                spyService.addChannelSpy(player.uniqueId, it)
            } else {
                player.sendPrefixed {
                    error("Du spionierst bereits in dem Kanal ${it.name}.")
                }
                return@playerExecutor
            }
        }

        player.sendPrefixed {
            success("Du spionierst jetzt in den Kanälen: ")
            variableValue(channels.joinToString(", ") { it.name })
        }
    }
}