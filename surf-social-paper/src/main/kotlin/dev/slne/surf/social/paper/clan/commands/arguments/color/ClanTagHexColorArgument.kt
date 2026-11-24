package dev.slne.surf.social.paper.clan.commands.arguments.color

import dev.slne.surf.social.core.client.clan.command.CLAN_TAG_COLOR_LENGTH
import net.kyori.adventure.text.format.TextColor

class ClanTagHexColorArgument(nodeName: String) :
    BaseHexColorArgument<TextColor>(nodeName, CLAN_TAG_COLOR_LENGTH, {
        TextColor.fromHexString("#$it")
    })
