package dev.slne.surf.minimapcompanion.core.xaero.option

import dev.slne.surf.minimapcompanion.core.xaero.nbt.*
import dev.slne.surf.minimapcompanion.core.xaero.option.XaeroOption.Companion.bool
import dev.slne.surf.minimapcompanion.core.xaero.option.XaeroOption.Companion.dimensions
import dev.slne.surf.minimapcompanion.core.xaero.option.XaeroOption.Companion.double
import dev.slne.surf.minimapcompanion.core.xaero.option.XaeroOption.Companion.indexed
import dev.slne.surf.minimapcompanion.core.xaero.option.XaeroOption.Companion.int
import dev.slne.surf.minimapcompanion.core.xaero.option.XaeroOption.Companion.positiveDouble
import dev.slne.surf.minimapcompanion.core.xaero.option.XaeroOption.Companion.string
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnforcedOptionsTest {
    private val catalog = XaeroOptionCatalog(
        bool("flag"),
        int("level", 0..3),
        double("scale"),
        positiveDouble("size"),
        string("command"),
        dimensions("dims"),
        indexed("km", -1..2) { if (it <= 0) it else 10 },
    )

    private val warnings = mutableListOf<String>()

    private fun parse(vararg raw: Pair<String, Any?>) = EnforcedOptions.parse(catalog, mapOf(*raw), warnings::add)

    @Test
    fun `known values keep their type`() {
        val options = parse("flag" to false, "level" to 2, "scale" to 1.5, "command" to "/tp")
        assertEquals(mapOf("flag" to false, "level" to 2, "scale" to 1.5, "command" to "/tp"), options.values)
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `integer for a double option becomes a double`() {
        assertEquals(mapOf<String, Any>("scale" to 2.0), parse("scale" to 2).values)
    }

    @Test
    fun `long outside int range is skipped with a warning`() {
        assertEquals(emptyMap(), parse("level" to 5_000_000_000L).values)
        assertEquals(1, warnings.size)
        assertTrue("level" in warnings.single())
    }

    @Test
    fun `long inside int range is accepted for an int option`() {
        assertEquals(mapOf<String, Any>("level" to 3), parse("level" to 3L).values)
    }

    @Test
    fun `value of the wrong type is skipped with a warning`() {
        assertEquals(emptyMap(), parse("flag" to "yes").values)
        assertTrue("flag" in warnings.single())
    }

    @Test
    fun `value outside the range is skipped with a warning`() {
        assertEquals(emptyMap(), parse("level" to 4, "size" to 0.0).values)
        assertEquals(2, warnings.size)
    }

    @Test
    fun `indexed option accepts only mapped values`() {
        assertEquals(mapOf<String, Any>("km" to 10), parse("km" to 10).values)
        assertEquals(emptyMap(), parse("km" to 5).values)
        assertTrue("km" in warnings.single())
    }

    @Test
    fun `null means not enforced`() {
        assertEquals(emptyMap(), parse("flag" to null).values)
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `dimensions accept a list or a comma separated string`() {
        val expected = setOf("minecraft:overworld", "minecraft:the_nether")
        assertEquals(expected, parse("dims" to listOf("minecraft:overworld", "minecraft:the_nether")).identifiers("dims"))
        assertEquals(expected, parse("dims" to "minecraft:overworld, minecraft:the_nether").identifiers("dims"))
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `invalid dimension key is skipped with a warning`() {
        assertEquals(emptyMap(), parse("dims" to listOf("Not A Key")).values)
        assertTrue("dims" in warnings.single())
    }

    @Test
    fun `unknown option is sent with an inferred type and a warning`() {
        val options = parse("new_flag" to true, "new_number" to 7, "new_scale" to 0.5, "new_list" to listOf("a:b"))
        assertEquals(
            mapOf("new_flag" to true, "new_number" to 7, "new_scale" to 0.5, "new_list" to setOf("a:b")),
            options.values,
        )
        assertEquals(4, warnings.size)
        assertTrue(warnings.all { "Unknown Xaero option" in it })
    }

    @Test
    fun `boolean reads only boolean values`() {
        val options = parse("flag" to false, "level" to 1)
        assertEquals(false, options.boolean("flag"))
        assertEquals(null, options.boolean("level"))
        assertEquals(null, options.boolean("missing"))
    }

    @Test
    fun `toNbt encodes each type with its tag`() {
        val options = parse(
            "flag" to true,
            "level" to 1,
            "scale" to 0.5,
            "command" to "/tp",
            "dims" to listOf("minecraft:overworld"),
        )
        assertEquals(
            nbtCompound(
                "flag" to NbtByte.of(true),
                "level" to NbtInt(1),
                "scale" to NbtDouble(0.5),
                "command" to NbtString("/tp"),
                "dims" to NbtList(listOf(NbtString("minecraft:overworld"))),
            ),
            options.toNbt(),
        )
    }
}
