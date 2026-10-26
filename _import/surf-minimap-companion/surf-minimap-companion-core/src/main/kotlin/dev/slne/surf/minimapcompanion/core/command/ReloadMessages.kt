package dev.slne.surf.minimapcompanion.core.command

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.minimapcompanion.core.config.CompanionReload
import net.kyori.adventure.audience.Audience

/**
 * Tells whoever ran the reload how long it took and how many Xaero options were not sent as configured.
 */
fun Audience.sendReloadResult(result: CompanionReload.Result) {
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
