package dev.slne.surf.player.core.client.minimap.command

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.minimap.config.ReloadResult
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

/** The feature name the minimap's reload messages report, the same as in `/surfplayer minimap`. */
private const val FEATURE = "minimap"

/**
 * The messages of a minimap reload: the shared reload message of every feature, plus how many options of the map
 * mods were not sent as configured; or the shared failure message naming the file that could not be read.
 */
fun reloadResultMessages(result: ReloadResult): List<Component> = when (result) {
    is ReloadResult.Failed -> listOf(
        CommandMessages.configReloadFailed(FEATURE, "${result.fileName}: ${result.reason}")
    )

    is ReloadResult.Reloaded -> {
        val reloaded = CommandMessages.configReloaded(FEATURE, result.duration.inWholeMilliseconds)
        val warnings = result.globalSettings.warnings.size + result.profile.warnings.size +
            result.journeyMapProfile.warnings.size + result.voxelMapProfile.warnings.size

        if (warnings == 0) {
            listOf(reloaded)
        } else {
            listOf(reloaded, buildText {
                appendWarningPrefix()
                variableValue("$warnings")
                warning(" Warnung(en) zu den Optionen der Karten-Mods, siehe Konsole.")
            })
        }
    }
}

/**
 * Tells whoever ran the reload the outcome, see [reloadResultMessages].
 */
fun Audience.sendReloadResult(result: ReloadResult) {
    reloadResultMessages(result).forEach(::sendMessage)
}
