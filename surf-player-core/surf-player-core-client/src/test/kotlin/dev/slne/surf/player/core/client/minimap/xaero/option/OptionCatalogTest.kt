package dev.slne.surf.player.core.client.minimap.xaero.option

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OptionCatalogTest {

    @Test
    fun `catalog ids are unique`() {
        for (catalog in listOf(MinimapOptions, WorldMapOptions)) {
            val ids = catalog.options.map { it.id }
            assertEquals(ids.size, ids.toSet().size)
        }
    }

    @Test
    fun `catalogs have the expected sizes`() {
        assertEquals(113, MinimapOptions.options.size)
        assertEquals(53, WorldMapOptions.options.size)
    }

    @Test
    fun `catalog rejects what xaero rejects`() {
        val uiScale = MinimapOptions["minimap_ui_scale"]!!
        assertTrue(uiScale.accepts(11))
        assertFalse(uiScale.accepts(12))

        val size = MinimapOptions["minimap_size"]!!
        assertTrue(size.accepts(0))
        assertFalse(size.accepts(54))
        assertTrue(size.accepts(55))
        assertTrue(size.accepts(250))
        assertFalse(size.accepts(251))

        assertFalse(MinimapOptions["radar_name_scale"]!!.accepts(0.0))
        assertTrue(MinimapOptions["minimap_chunk_grid"]!!.accepts(-16))
        assertTrue(WorldMapOptions["convert_map_distances_to_km_at"]!!.accepts(1000))
        assertFalse(WorldMapOptions["convert_map_distances_to_km_at"]!!.accepts(500))
    }

    @Test
    fun `complex minimap options are left out`() {
        assertEquals(null, MinimapOptions["entity_radar_categories"])
        assertEquals(null, MinimapOptions["info_display_config"])
    }

    @Test
    fun `cave mode dimensions are identifier sets`() {
        assertEquals(XaeroOptionType.IDENTIFIER_SET, MinimapOptions["minimap_cave_mode_allowed_dimensions"]!!.type)
        assertEquals(XaeroOptionType.IDENTIFIER_SET, WorldMapOptions["cave_mode_allowed_dimensions"]!!.type)
    }
}
