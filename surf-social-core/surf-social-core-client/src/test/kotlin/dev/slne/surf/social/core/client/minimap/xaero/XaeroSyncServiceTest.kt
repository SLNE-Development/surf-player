package dev.slne.surf.minimapcompanion.core.xaero

import dev.slne.surf.minimapcompanion.core.config.GlobalSettingsConfig
import dev.slne.surf.minimapcompanion.core.config.XaeroConfig
import dev.slne.surf.minimapcompanion.core.config.MinimapCompanionConfig
import dev.slne.surf.minimapcompanion.core.service.FakePlayer
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.Test
import org.spongepowered.configurate.BasicConfigurationNode
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class XaeroSyncServiceTest {
    private val config = MinimapCompanionConfig()
    private val xaero = XaeroConfig()

    private fun sync(
        player: FakePlayer,
        channels: Collection<String> = XaeroChannels.ALL,
        config: MinimapCompanionConfig = this.config,
        xaero: XaeroConfig = this.xaero,
    ) = XaeroSyncService.sync(player, channels, config, xaero)

    private val lobbyLevelId = XaeroPackets.levelId(829346366)

    private fun lobbyPlayer(channels: Set<String> = XaeroChannels.ALL.toSet()) =
        FakePlayer(worldIdentity = "surf:space_lobby", worldKey = Key.key("surf:space_lobby"), channels = channels)

    private fun lib(payload: ByteArray) = XaeroChannels.LIB to payload.toList()

    private fun configChannel(channel: String, options: CompoundBinaryTag = CompoundBinaryTag.empty()) = listOf(
        lib(XaeroLibPackets.configChannel(channel)),
        lib(XaeroLibPackets.profileInfoReset()),
        lib(XaeroLibPackets.enforcedConfig(options, reset = true)),
    )

    @Test
    fun `sync sends handshake, rules and level id on each map channel`() {
        val player = lobbyPlayer()
        sync(player, XaeroChannels.MAPS)
        assertEquals(
            listOf(
                XaeroChannels.WORLD_MAP to XaeroPackets.handshake().toList(),
                XaeroChannels.WORLD_MAP to XaeroPackets.worldMapRules(caveMode = true, netherCaveMode = true).toList(),
                XaeroChannels.WORLD_MAP to lobbyLevelId.toList(),
                XaeroChannels.MINIMAP to XaeroPackets.handshake().toList(),
                XaeroChannels.MINIMAP to XaeroPackets.minimapRules(caveMode = true, netherCaveMode = true, radar = true).toList(),
                XaeroChannels.MINIMAP to lobbyLevelId.toList(),
            ),
            player.sentContent,
        )
    }

    @Test
    fun `sync limited to one channel only sends on that channel`() {
        val player = FakePlayer()
        sync(player, listOf(XaeroChannels.MINIMAP))
        assertEquals(listOf(XaeroChannels.MINIMAP), player.sent.map { it.first }.distinct())
    }

    @Test
    fun `lib sync sends the handshakes and every config channel`() {
        val player = lobbyPlayer()
        sync(player, listOf(XaeroChannels.LIB))
        assertEquals(
            listOf(
                lib(XaeroLibPackets.dimensionHandshake()),
                lib(XaeroLibPackets.serverHandshake()),
            ) + configChannel(XaeroChannels.LIB) + configChannel(XaeroChannels.MINIMAP) + configChannel(XaeroChannels.WORLD_MAP),
            player.sentContent,
        )
    }

    @Test
    fun `full sync sends the map channels before the lib sequence`() {
        val player = lobbyPlayer()
        sync(player)
        val channels = player.sent.map { it.first }
        assertEquals(listOf(XaeroChannels.WORLD_MAP, XaeroChannels.MINIMAP, XaeroChannels.LIB), channels.distinct())
        assertEquals(6, channels.count(XaeroChannels::isMap))
    }

    @Test
    fun `lib sync skips config channels of mods the client lacks`() {
        val player = lobbyPlayer(channels = setOf(XaeroChannels.LIB, XaeroChannels.WORLD_MAP))
        sync(player, listOf(XaeroChannels.LIB))
        val selected = player.sentContent.filter { it.first == XaeroChannels.LIB && it.second.first() == 2.toByte() }
        assertEquals(
            listOf(XaeroChannels.LIB, XaeroChannels.WORLD_MAP).map { lib(XaeroLibPackets.configChannel(it)) },
            selected,
        )
    }

    @Test
    fun `enforced options reach the client`() {
        val player = lobbyPlayer()
        sync(player, xaero = XaeroConfig(minimap = mapOf("display_radar" to BasicConfigurationNode.root().raw(false))))

        val sent = player.sentContent
        assertTrue(XaeroChannels.MINIMAP to XaeroPackets.minimapRules(caveMode = true, netherCaveMode = true, radar = false).toList() in sent)
        assertTrue(lib(XaeroLibPackets.enforcedConfig(CompoundBinaryTag.builder().putBoolean("display_radar", false).build(), reset = true)) in sent)
    }

    @Test
    fun `sync applies the global settings of config yml`() {
        val player = lobbyPlayer()
        sync(player, config = MinimapCompanionConfig(globalSettings = GlobalSettingsConfig(radar = BasicConfigurationNode.root().raw(false))))

        val sent = player.sentContent
        assertTrue(XaeroChannels.MINIMAP to XaeroPackets.minimapRules(caveMode = true, netherCaveMode = true, radar = false).toList() in sent)
        assertTrue(lib(XaeroLibPackets.enforcedConfig(CompoundBinaryTag.builder().putBoolean("display_radar", false).build(), reset = true)) in sent)
    }

    @Test
    fun `sync remembers the last sent level id per player`() {
        val player = lobbyPlayer()
        sync(player)
        assertEquals(829346366, XaeroSyncService.lastSentLevelId(player.uuid))
    }

    @Test
    fun `lib only sync sends no level id, so none is remembered`() {
        val player = lobbyPlayer()
        sync(player, listOf(XaeroChannels.LIB))
        assertNull(XaeroSyncService.lastSentLevelId(player.uuid))
    }

    @Test
    fun `sync uses the configured override for the world`() {
        val player = FakePlayer(worldKey = Key.key("minecraft:overworld"))
        sync(player, config = MinimapCompanionConfig(worldIds = mapOf("minecraft:overworld" to 7)))
        assertEquals(7, XaeroSyncService.lastSentLevelId(player.uuid))
    }

    @Test
    fun `sync hides the maps while disable-maps is on`() {
        val player = lobbyPlayer()
        sync(player, config = MinimapCompanionConfig(disableMaps = true))
        assertEquals(setOf(40, 48), player.addedEffects.toSet())
    }

    @Test
    fun `sync leaves the maps alone by default`() {
        val player = lobbyPlayer()
        sync(player)
        assertEquals(emptyList(), player.addedEffects)
    }

    @Test
    fun `forget removes the last sent level id`() {
        val player = FakePlayer()
        sync(player)
        XaeroSyncService.forget(player.uuid)
        assertNull(XaeroSyncService.lastSentLevelId(player.uuid))
    }
}
