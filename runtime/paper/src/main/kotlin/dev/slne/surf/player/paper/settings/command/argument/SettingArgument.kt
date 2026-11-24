package dev.slne.surf.player.paper.settings.command.argument

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.player.api.settings.setting.Setting
import dev.slne.surf.player.core.client.settings.command.SettingsCommandMessages
import dev.slne.surf.player.core.client.settings.service.SettingsService

class SettingArgument(nodeName: String) :
    CustomArgument<Setting, String>(StringArgument(nodeName), { info ->
        SettingsService.getSettingByName(info.input)
            ?: throw CustomArgumentException.fromAdventureComponent(
                SettingsCommandMessages.settingNotFound
            )
    }) {
    init {
        this.replaceSuggestions(
            ArgumentSuggestions.stringCollection {
                SettingsService.settings.map { it.name }
            }
        )
    }
}

inline fun CommandTree.settingArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(
    SettingArgument(nodeName).setOptional(optional).apply(block)
)

inline fun Argument<*>.settingArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(
    SettingArgument(nodeName).setOptional(optional).apply(block)
)

inline fun CommandAPICommand.settingArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(SettingArgument(nodeName).setOptional(optional).apply(block))
