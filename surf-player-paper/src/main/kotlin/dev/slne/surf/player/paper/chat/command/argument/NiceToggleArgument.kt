package dev.slne.surf.player.paper.chat.command.argument

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.api.core.messages.adventure.buildText

class NiceToggleArgument(nodeName: String) :
    CustomArgument<Boolean, String>(StringArgument(nodeName), { info ->
        when (info.input()) {
            "enable", "on", "an" -> true
            "disable", "off", "aus" -> false
            else -> throw CustomArgumentException.fromAdventureComponent {
                buildText {
                    appendErrorPrefix()
                    error("Bitte gebe entweder 'enable', 'disable', 'on' oder 'off' an.")
                }
            }
        }
    }) {
    init {
        this.replaceSuggestions(
            ArgumentSuggestions.strings(
                "enable",
                "disable",
                "on",
                "off",
                "an",
                "aus"
            )
        )
    }
}

fun CommandAPICommand.niceToggleArgument(nodeName: String): CommandAPICommand =
    withArguments(NiceToggleArgument(nodeName))