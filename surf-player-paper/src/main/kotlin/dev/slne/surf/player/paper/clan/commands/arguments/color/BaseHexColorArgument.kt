package dev.slne.surf.player.paper.clan.commands.arguments.color

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.TextArgument
import dev.slne.surf.player.core.client.clan.command.INVALID_HEX_COLOR
import dev.slne.surf.player.core.client.clan.command.isHexColor
import dev.slne.surf.player.paper.clan.commands.failCommand

/**
 * Parses a hex color of [requiredLength] digits (without `#`) via [factory].
 */
abstract class BaseHexColorArgument<T>(
    nodeName: String,
    requiredLength: Int,
    suggestions: Collection<String>,
    factory: (String) -> T?
) : CustomArgument<T, String>(TextArgument(nodeName), { info ->
    val input = info.currentInput().trim()
    val color = if (isHexColor(input, requiredLength)) factory(input) else null

    color ?: failCommand("$INVALID_HEX_COLOR ${info.currentInput}")
}) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollection { _ -> suggestions })
    }
}
