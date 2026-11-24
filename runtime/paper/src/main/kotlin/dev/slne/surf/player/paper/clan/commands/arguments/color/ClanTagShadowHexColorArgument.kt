package dev.slne.surf.player.paper.clan.commands.arguments.color

import dev.slne.surf.player.core.client.clan.command.CLAN_TAG_SHADOW_COLOR_LENGTH
import dev.slne.surf.player.core.client.clan.command.CLAN_TAG_SHADOW_COLOR_SUGGESTIONS
import net.kyori.adventure.text.format.ShadowColor

class ClanTagShadowHexColorArgument(nodeName: String) :
    BaseHexColorArgument<ShadowColor>(nodeName, CLAN_TAG_SHADOW_COLOR_LENGTH, CLAN_TAG_SHADOW_COLOR_SUGGESTIONS, {
        ShadowColor.fromHexString("#$it")
    })
