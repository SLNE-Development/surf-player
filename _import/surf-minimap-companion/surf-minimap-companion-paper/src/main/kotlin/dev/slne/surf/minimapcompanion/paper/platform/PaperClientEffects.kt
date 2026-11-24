package dev.slne.surf.minimapcompanion.paper.platform

import com.github.retrooper.packetevents.protocol.player.ClientVersion
import com.github.retrooper.packetevents.protocol.potion.PotionType
import com.github.retrooper.packetevents.resources.ResourceLocation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect
import dev.slne.surf.api.core.extensions.packetEvents
import dev.slne.surf.api.core.extensions.sendPacket
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player

/**
 * Sends status effects only the client knows through PacketEvents, which surf-api brings along.
 * Paper's own effect API only takes effects the server registered.
 */
object PaperClientEffects {
    private const val INFINITE_DURATION = -1
    private const val AMBIENT: Byte = 0x01

    // Minecraft's own effects take the first IDs, so the effects mods add start after them.
    private val vanillaEffectCount by lazy {
        RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT).count { it.key.namespace == NamespacedKey.MINECRAFT }
    }

    /**
     * Returns where the IDs of the effects mods add start on [player]'s client, or `null` if PacketEvents does
     * not see the client on the server's version, as with a client joined through ViaVersion.
     */
    fun vanillaEffectCount(player: Player): Int? {
        if (packetEvents.playerManager.getClientVersion(player) != packetEvents.serverManager.version.toClientVersion()) return null
        return vanillaEffectCount
    }

    fun add(player: Player, networkId: Int) {
        val effect = ClientOnlyEffect(networkId)
        WrapperPlayServerEntityEffect(player.entityId, effect, 0, INFINITE_DURATION, AMBIENT).sendPacket(player)
    }

    fun remove(player: Player, networkId: Int) {
        val effect = ClientOnlyEffect(networkId)
        WrapperPlayServerRemoveEntityEffect(player.entityId, effect).sendPacket(player)
    }

    /**
     * An effect PacketEvents writes by [networkId] alone, without looking it up in a registry.
     */
    private class ClientOnlyEffect(private val networkId: Int) : PotionType {
        override fun getName() = ResourceLocation("surf-minimap-companion", "client_effect_$networkId")

        override fun getId(version: ClientVersion) = networkId
    }
}
