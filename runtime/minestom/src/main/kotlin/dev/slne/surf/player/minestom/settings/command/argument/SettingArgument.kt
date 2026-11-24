package dev.slne.surf.player.minestom.settings.command.argument

import dev.slne.surf.api.minestom.command.CommandAPI
import dev.slne.surf.api.minestom.command.CommandTree
import dev.slne.surf.api.minestom.command.argument.Argument
import dev.slne.surf.api.minestom.command.argument.CustomArgument
import dev.slne.surf.api.minestom.command.argument.ResourceLocationArgument
import dev.slne.surf.api.minestom.command.suggestion.ArgumentSuggestions
import dev.slne.surf.player.api.settings.setting.Setting
import dev.slne.surf.player.core.client.settings.command.SettingsCommandMessages
import dev.slne.surf.player.core.client.settings.service.SettingsService
import net.kyori.adventure.key.Key

/**
 * A registered setting, typed as its namespaced key (`chat:pings`). A word argument would stop at
 * the `:`, so the key is read as a resource location and resolved by
 * [SettingsService.findSettingByInput].
 */
class SettingArgument(nodeName: String) :
    CustomArgument<Setting, Key>(ResourceLocationArgument(nodeName), { info ->
        SettingsService.findSettingByInput(info.currentInput)
            ?: CommandAPI.failWithMessage(SettingsCommandMessages.settingNotFound)
    }) {
    init {
        replaceSuggestions(
            ArgumentSuggestions.stringCollection { info ->
                SettingsService.suggestSettingNames(info.currentArg)
            }
        )
    }
}

inline fun CommandTree.settingArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<Setting>.() -> Unit = {}
): CommandTree = then(
    SettingArgument(nodeName).setOptional(optional = optional).apply(block)
)
