package dev.slne.surf.minimapcompanion.core.service

import dev.slne.surf.minimapcompanion.core.config.MinimapCompanionConfig
import dev.slne.surf.minimapcompanion.core.xaero.XaeroPackets
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class XaeroSyncServiceTest {
    private val config = MinimapCompanionConfig()

    @Test
    fun `sync sends the level id on both xaero channels`() {
        val player = FakePlayer(worldIdentity = "surf:space_lobby", worldKey = Key.key("surf:space_lobby"))
        XaeroSyncService.sync(player, config = config)
        val payload = XaeroPackets.levelId(829346366).toList()
        assertEquals(
            listOf("xaeroworldmap:main" to payload, "xaerominimap:main" to payload),
            player.sent.map { (channel, bytes) -> channel to bytes.toList() },
        )
    }

    @Test
    fun `sync limited to one channel only sends on that channel`() {
        val player = FakePlayer()
        XaeroSyncService.sync(player, listOf("xaerominimap:main"), config)
        assertEquals(listOf("xaerominimap:main"), player.sent.map { it.first })
    }

    @Test
    fun `sync remembers the last sent level id per player`() {
        val player = FakePlayer(worldIdentity = "surf:space_lobby", worldKey = Key.key("surf:space_lobby"))
        XaeroSyncService.sync(player, config = config)
        assertEquals(829346366, XaeroSyncService.lastSentLevelId(player.uuid))
    }

    @Test
    fun `sync uses the configured override for the world`() {
        val player = FakePlayer(worldKey = Key.key("minecraft:overworld"))
        XaeroSyncService.sync(player, config = MinimapCompanionConfig(worldIds = mapOf("minecraft:overworld" to 7)))
        assertEquals(7, XaeroSyncService.lastSentLevelId(player.uuid))
    }

    @Test
    fun `forget removes the last sent level id`() {
        val player = FakePlayer()
        XaeroSyncService.sync(player, config = config)
        XaeroSyncService.forget(player.uuid)
        assertNull(XaeroSyncService.lastSentLevelId(player.uuid))
    }
}
