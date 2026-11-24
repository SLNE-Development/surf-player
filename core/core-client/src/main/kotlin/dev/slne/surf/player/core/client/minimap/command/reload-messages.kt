package dev.slne.surf.player.core.client.minimap.command

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.player.core.client.minimap.config.ReloadResult
import net.kyori.adventure.audience.Audience

/**
 * Tells whoever ran the reload how long it took and how many options of the map mods were not sent as configured,
 * or which file could not be read.
 */
fun Audience.sendReloadResult(result: ReloadResult) {
    when (result) {
        is ReloadResult.Failed -> sendText {
            appendErrorPrefix()
            variableValue(result.fileName)
            error(" konnte nicht gelesen werden, die bisherige Konfiguration bleibt aktiv: ")
            variableValue(result.reason)
        }

        is ReloadResult.Reloaded -> {
            sendText {
                appendSuccessPrefix()
                success("Die Konfiguration wurde neu geladen (${result.duration.inWholeMilliseconds}ms).")
            }

            val warnings = result.globalSettings.warnings.size + result.profile.warnings.size +
                result.journeyMapProfile.warnings.size + result.voxelMapProfile.warnings.size
            if (warnings == 0) return

            sendText {
                appendWarningPrefix()
                variableValue("$warnings")
                warning(" Warnung(en) zu den Optionen der Karten-Mods, siehe Konsole.")
            }
        }
    }
}
