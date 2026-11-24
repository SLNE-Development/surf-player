package dev.slne.surf.player.minestom.chat.command.argument

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.CommandTree
import dev.slne.surf.api.minestom.command.argument.Argument
import dev.slne.surf.api.minestom.command.argument.CustomArgument
import dev.slne.surf.api.minestom.command.argument.StringArgument
import dev.slne.surf.api.minestom.command.suggestion.ArgumentSuggestions
import dev.slne.surf.player.minestom.command.failCommand

class NiceToggleArgument(nodeName: String) :
    CustomArgument<Boolean, String>(StringArgument(nodeName), { info ->
        when (info.currentInput) {
            "enable", "on", "an" -> true
            "disable", "off", "aus" -> false
            else -> failCommand("Bitte gebe entweder 'enable', 'disable', 'on' oder 'off' an.")
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

inline fun CommandAPICommand.niceToggleArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<Boolean>.() -> Unit = {}
): CommandAPICommand =
    withArguments(NiceToggleArgument(nodeName).setOptional(optional = optional).apply(block))

inline fun CommandTree.niceToggleArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<Boolean>.() -> Unit = {}
): CommandTree =
    then(NiceToggleArgument(nodeName).setOptional(optional = optional).apply(block))
