package dev.slne.surf.minimapcompanion.core.command

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.minimapcompanion.core.config.CompanionConfigs.ReloadResult
import net.kyori.adventure.audience.Audience

/**
 * Tells whoever ran the reload how long it took and how many Xaero options were not sent as configured,
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

            val warnings = result.profile.warnings.size
            if (warnings == 0) return

            sendText {
                appendWarningPrefix()
                variableValue("$warnings")
                warning(" Warnung(en) zu den Xaero-Optionen, siehe Konsole.")
            }
        }
    }
}
