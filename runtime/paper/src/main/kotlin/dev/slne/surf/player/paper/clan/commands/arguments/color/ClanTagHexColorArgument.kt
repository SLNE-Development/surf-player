package dev.slne.surf.player.paper.clan.commands.arguments.color

import dev.slne.surf.player.core.client.clan.command.CLAN_TAG_COLOR_LENGTH
import dev.slne.surf.player.core.client.clan.command.CLAN_TAG_COLOR_SUGGESTIONS
import net.kyori.adventure.text.format.TextColor

class ClanTagHexColorArgument(nodeName: String) :
    BaseHexColorArgument<TextColor>(nodeName, CLAN_TAG_COLOR_LENGTH, CLAN_TAG_COLOR_SUGGESTIONS, {
        TextColor.fromHexString("#$it")
    })
