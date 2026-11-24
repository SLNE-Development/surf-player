package dev.slne.surf.social.core.client.minimap.command

import dev.slne.surf.api.core.messages.adventure.sendText
import net.kyori.adventure.audience.Audience

/**
 * Tells whoever ran `/minimapcompanion info` what the companion knows and sends about the player in [info].
 */
fun Audience.sendMapInfo(info: MapInfo) = sendText {
    appendInfoPrefix()
    info("Karten-Info für ")
    variableValue(info.playerName)

    appendNewInfoPrefixedLine()
    variableKey("Welt: ")
    variableValue(info.world)

    appendNewInfoPrefixedLine()
    variableKey("Level-ID: ")
    variableValue("${info.levelId}")
    info(" (zuletzt an Xaero gesendet: ")
    when (info.lastSentLevelId) {
        null -> info("noch keine")
        info.levelId -> success("${info.lastSentLevelId}")
        else -> error("${info.lastSentLevelId}")
    }
    info(")")

    appendNewInfoPrefixedLine()
    variableKey("Welt-ID für JourneyMap/VoxelMap: ")
    variableValue(info.worldName)

    appendNewInfoPrefixedLine()
    variableKey("Mods: ")
    info.mods.forEachIndexed { index, (mod, installed) ->
        if (index > 0) spacer("  ")
        if (installed) success("${mod.displayName} ✔") else error("${mod.displayName} ✘")
    }

    appendNewInfoPrefixedLine()
    variableKey("disable-maps: ")
    if (info.disableMaps) warning("an") else variableValue("aus")

    appendNewInfoPrefixedLine()
    variableKey("Erzwungen: ")
    variableValue("Xaero ${info.xaeroOptions} Optionen")
    spacer(" · ")
    variableValue("JourneyMap ${info.journeyMapOptions}")
    spacer(" · ")
    variableValue("VoxelMap ${info.voxelMapOptions}")
    if (info.globalOverrides.isNotEmpty()) {
        spacer(" · ")
        info("global: ")
        variableValue(info.globalOverrides.entries.joinToString(", ") { (feature, allowed) -> "${feature.key}=$allowed" })
    }

    val tracker = info.tracker ?: return@sendText
    appendNewInfoPrefixedLine()
    variableKey("Tracker: ")
    variableValue(tracker.mode.displayName)
    spacer(" · ")
    variableValue("${tracker.xaeroPlayers}")
    info(" Spieler auf Xaeros Karte gesendet")
}
