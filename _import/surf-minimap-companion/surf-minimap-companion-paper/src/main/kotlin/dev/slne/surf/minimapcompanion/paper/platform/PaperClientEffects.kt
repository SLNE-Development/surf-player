package dev.slne.surf.minimapcompanion.paper.platform

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.player.ClientVersion
import com.github.retrooper.packetevents.protocol.potion.PotionType
import com.github.retrooper.packetevents.resources.ResourceLocation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRemoveEntityEffect
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
        val api = PacketEvents.getAPI()
        if (api.playerManager.getClientVersion(player) != api.serverManager.version.toClientVersion()) return null
        return vanillaEffectCount
    }

    fun add(player: Player, networkId: Int) {
        val effect = ClientOnlyEffect(networkId)
        PacketEvents.getAPI().playerManager.sendPacket(
            player,
            WrapperPlayServerEntityEffect(player.entityId, effect, 0, INFINITE_DURATION, AMBIENT),
        )
    }

    fun remove(player: Player, networkId: Int) {
        val effect = ClientOnlyEffect(networkId)
        PacketEvents.getAPI().playerManager.sendPacket(player, WrapperPlayServerRemoveEntityEffect(player.entityId, effect))
    }

    /**
     * An effect PacketEvents writes by [networkId] alone, without looking it up in a registry.
     */
    private class ClientOnlyEffect(private val networkId: Int) : PotionType {
        override fun getName() = ResourceLocation("surf-minimap-companion", "client_effect_$networkId")

        override fun getId(version: ClientVersion) = networkId
    }
}
