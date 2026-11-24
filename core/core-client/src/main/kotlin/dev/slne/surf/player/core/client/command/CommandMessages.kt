package dev.slne.surf.player.core.client.command

import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextDecoration

/**
 * The texts every command uses for the same situation, so that "unknown player", "player not
 * online", missing permissions and so on read the same in every feature and on every platform.
 */
object CommandMessages {
    const val PLAYER_NOT_FOUND = "Der Spieler wurde nicht gefunden."
    const val PLAYER_NOT_ONLINE = "Der Spieler ist nicht online."
    const val NO_PERMISSION = "Dazu hast du keine Berechtigung."
    const val PLAYERS_ONLY = "Diesen Befehl können nur Spieler ausführen."
    const val NOT_YOURSELF = "Das kannst du nicht mit dir selbst tun."
    const val NOTHING_AVAILABLE = "Hier gibt es für dich nichts zu tun."
    const val CONFIRMATION_EXPIRED = "Die Bestätigung ist abgelaufen. Bitte führe den Befehl erneut aus."
    const val SERVICE_UNAVAILABLE = "Der Dienst ist gerade nicht erreichbar. Bitte versuche es später erneut."

    /**
     * Reports that the configuration of [feature] was read from disk again, which took [millis].
     */
    fun configReloaded(feature: String, millis: Long): Component = buildText {
        appendSuccessPrefix()
        success("Die Konfiguration von ")
        variableValue(feature)
        success(" wurde neu geladen (")
        variableValue("${millis}ms")
        success(").")
    }

    /**
     * Reports that reading the configuration of [feature] failed, optionally with the [reason];
     * the old configuration stays active.
     */
    fun configReloadFailed(feature: String, reason: String? = null): Component = buildText {
        appendErrorPrefix()
        error("Die Konfiguration von ")
        variableValue(feature)
        error(" konnte nicht neu geladen werden. Die bisherige Konfiguration bleibt aktiv.")
        if (reason != null) {
            appendSpace()
            spacer("(")
            variableValue(reason)
            spacer(")")
        }
    }

    /**
     * Asks the sender to confirm a destructive action: [question] is followed by a button that
     * runs [click]; [warning] is shown when hovering the button.
     */
    fun confirmation(question: Component, warning: String, click: ClickEvent<*>): Component = buildText {
        appendWarningPrefix()
        append(question)
        appendSpace()
        append {
            error("[Bestätigen]", TextDecoration.BOLD)
            hoverEvent(buildText {
                error("Achtung: ", TextDecoration.BOLD)
                error(warning)
                appendNewline(2)
                info("Klicke, um zu bestätigen.")
            })
            clickEvent(click)
        }
    }
}
