package dev.slne.surf.player.tests.bot

import org.cloudburstmc.nbt.NbtList
import org.cloudburstmc.nbt.NbtMap
import org.cloudburstmc.nbt.NbtMapBuilder
import org.cloudburstmc.nbt.NbtType

/**
 * Minimal SNBT reader for the payloads of custom click events (Paper's `ClickEvent.callback`
 * sends e.g. `{id:[I;1,2,3,4]}`), so a bot can send the payload back unchanged.
 * Supports compounds, lists, int/byte/long arrays, strings and numbers.
 */
class Snbt private constructor(private val s: String) {
    private var i = 0

    companion object {
        fun parseCompound(text: String): NbtMap = Snbt(text.trim()).compound()
    }

    private fun ws() { while (i < s.length && s[i].isWhitespace()) i++ }
    private fun peek(): Char { ws(); return s[i] }
    private fun expect(c: Char) { ws(); check(s[i] == c) { "expected '$c' at $i in $s" }; i++ }

    private fun compound(): NbtMap {
        expect('{')
        val b: NbtMapBuilder = NbtMap.builder()
        if (peek() == '}') { i++; return b.build() }
        while (true) {
            val key = key()
            expect(':')
            b.put(key, value())
            if (peek() == ',') { i++; continue }
            expect('}')
            return b.build()
        }
    }

    private fun key(): String {
        ws()
        if (s[i] == '"' || s[i] == '\'') return quoted()
        val start = i
        while (i < s.length && (s[i].isLetterOrDigit() || s[i] in "_-.+")) i++
        return s.substring(start, i)
    }

    private fun quoted(): String {
        val q = s[i++]
        val sb = StringBuilder()
        while (s[i] != q) {
            if (s[i] == '\\') i++
            sb.append(s[i++])
        }
        i++
        return sb.toString()
    }

    private fun value(): Any {
        return when (val c = peek()) {
            '{' -> compound()
            '[' -> list()
            '"', '\'' -> quoted()
            else -> number(c)
        }
    }

    private fun list(): Any {
        expect('[')
        ws()
        if (i + 1 < s.length && s[i + 1] == ';' && s[i] in "IBL") {
            val type = s[i]; i += 2
            val items = ArrayList<String>()
            while (peek() != ']') {
                val start = i
                while (s[i] != ',' && s[i] != ']') i++
                items += s.substring(start, i).trim().trimEnd('b', 'B', 'l', 'L')
                if (s[i] == ',') i++
            }
            i++
            return when (type) {
                'I' -> items.map { it.toInt() }.toIntArray()
                'B' -> items.map { it.toByte() }.toByteArray()
                else -> items.map { it.toLong() }.toLongArray()
            }
        }
        val values = ArrayList<Any>()
        while (peek() != ']') {
            values += value()
            if (peek() == ',') i++
        }
        i++
        @Suppress("UNCHECKED_CAST")
        return when (values.firstOrNull()) {
            is NbtMap -> NbtList(NbtType.COMPOUND, values as List<NbtMap>)
            is String -> NbtList(NbtType.STRING, values as List<String>)
            is Int -> NbtList(NbtType.INT, values as List<Int>)
            else -> NbtList(NbtType.STRING, values.map { it.toString() })
        }
    }

    private fun number(first: Char): Any {
        val start = i
        while (i < s.length && s[i] !in ",}]") i++
        val raw = s.substring(start, i).trim()
        return when {
            raw == "true" -> 1.toByte()
            raw == "false" -> 0.toByte()
            raw.endsWith("b", true) -> raw.dropLast(1).toByte()
            raw.endsWith("s", true) -> raw.dropLast(1).toShort()
            raw.endsWith("l", true) -> raw.dropLast(1).toLong()
            raw.endsWith("f", true) -> raw.dropLast(1).toFloat()
            raw.endsWith("d", true) -> raw.dropLast(1).toDouble()
            raw.contains('.') -> raw.toDouble()
            raw.toIntOrNull() != null -> raw.toInt()
            else -> raw
        }
    }
}
