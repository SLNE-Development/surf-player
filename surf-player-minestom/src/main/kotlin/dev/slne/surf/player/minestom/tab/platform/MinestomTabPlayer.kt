package dev.slne.surf.player.minestom.tab.platform

import dev.slne.surf.api.minestom.coroutine.withEntity
import dev.slne.surf.player.core.client.tab.platform.TabPlayer
import dev.slne.surf.player.core.client.util.prefixedName
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import java.util.*

class MinestomTabPlayer(private val player: Player) : TabPlayer {
    override val uuid: UUID get() = player.uuid
    override val audience get() = player

    override fun baseName(): Component = prefixedName(player.uuid, player.username)

    override suspend fun baseNameSnapshot(): Component = player.withEntity { baseName() }

    override suspend fun showTabEntry(name: Component, order: Int) {
        player.withEntity {
            it.displayName = name
            it.listOrder = order
        }
    }
}
