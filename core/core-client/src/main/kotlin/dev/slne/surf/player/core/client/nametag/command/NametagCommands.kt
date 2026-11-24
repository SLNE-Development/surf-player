package dev.slne.surf.player.core.client.nametag.command

import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.nametag.config.NametagConfig
import net.kyori.adventure.text.Component
import kotlin.system.measureTimeMillis

/**
 * The nametag admin actions every platform runs.
 */
object NametagCommands {
    /** Reads the nametag configuration again; it applies to nametags updated from now on. */
    fun reload(): Component {
        val millis = measureTimeMillis { NametagConfig.reloadFromFile() }
        return CommandMessages.configReloaded("nametag", millis)
    }
}
