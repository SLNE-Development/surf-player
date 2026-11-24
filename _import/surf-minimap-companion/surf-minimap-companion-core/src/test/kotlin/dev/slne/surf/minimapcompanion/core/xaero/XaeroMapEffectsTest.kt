package dev.slne.surf.minimapcompanion.core.xaero

import dev.slne.surf.minimapcompanion.core.service.FakePlayer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class XaeroMapEffectsTest {
    private val vanillaEffects = 40

    // The client registers the minimap's eight effects right after Minecraft's own, then the world map's four.
    private val noMinimap = 40
    private val noWorldMapAfterMinimap = 48
    private val noWorldMapAlone = 40

    private fun player(vararg channels: String, vanillaEffectCount: Int? = vanillaEffects) =
        FakePlayer(channels = channels.toSet(), vanillaEffectCount = vanillaEffectCount)

    @Test
    fun `disabled maps hide both maps on a client with both mods`() {
        val player = player(XaeroChannels.MINIMAP, XaeroChannels.WORLD_MAP, XaeroChannels.LIB)
        XaeroMapEffects.sync(player, disableMaps = true)
        assertEquals(setOf(noMinimap, noWorldMapAfterMinimap), player.addedEffects.toSet())
    }

    @Test
    fun `world map alone gets its effect right after minecraft's own`() {
        val player = player(XaeroChannels.WORLD_MAP, XaeroChannels.LIB)
        XaeroMapEffects.sync(player, disableMaps = true)
        assertEquals(listOf(noWorldMapAlone), player.addedEffects)
    }

    @Test
    fun `minimap alone gets only the no minimap effect`() {
        val player = player(XaeroChannels.MINIMAP, XaeroChannels.LIB)
        XaeroMapEffects.sync(player, disableMaps = true)
        assertEquals(listOf(noMinimap), player.addedEffects)
    }

    @Test
    fun `client without xaero's maps gets no effects`() {
        val player = player(XaeroChannels.LIB)
        XaeroMapEffects.sync(player, disableMaps = true)
        assertEquals(emptyList(), player.addedEffects)
    }

    @Test
    fun `client whose effect ids are unknown gets no effects`() {
        val player = player(XaeroChannels.MINIMAP, XaeroChannels.WORLD_MAP, vanillaEffectCount = null)
        XaeroMapEffects.sync(player, disableMaps = true)
        assertEquals(emptyList(), player.addedEffects)
    }

    @Test
    fun `every sync gives the effects again, as respawns and world changes clear them on the client`() {
        val player = player(XaeroChannels.MINIMAP)
        XaeroMapEffects.sync(player, disableMaps = true)
        XaeroMapEffects.sync(player, disableMaps = true)
        assertEquals(listOf(noMinimap, noMinimap), player.addedEffects)
    }

    @Test
    fun `turning disable-maps off takes back the effects given before`() {
        val player = player(XaeroChannels.MINIMAP, XaeroChannels.WORLD_MAP)
        XaeroMapEffects.sync(player, disableMaps = true)
        XaeroMapEffects.sync(player, disableMaps = false)
        assertEquals(setOf(noMinimap, noWorldMapAfterMinimap), player.removedEffects.toSet())

        XaeroMapEffects.sync(player, disableMaps = false)
        assertEquals(2, player.removedEffects.size)
    }

    @Test
    fun `maps left on touch no effects`() {
        val player = player(XaeroChannels.MINIMAP, XaeroChannels.WORLD_MAP)
        XaeroMapEffects.sync(player, disableMaps = false)
        assertEquals(emptyList(), player.addedEffects)
        assertEquals(emptyList(), player.removedEffects)
    }

    @Test
    fun `forgotten player has nothing to take back`() {
        val player = player(XaeroChannels.MINIMAP)
        XaeroMapEffects.sync(player, disableMaps = true)
        XaeroMapEffects.forget(player.uuid)
        XaeroMapEffects.sync(player, disableMaps = false)
        assertEquals(emptyList(), player.removedEffects)
    }
}
