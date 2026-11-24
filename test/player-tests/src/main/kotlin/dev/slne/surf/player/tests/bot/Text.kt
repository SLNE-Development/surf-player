package dev.slne.surf.player.tests.bot

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

/** Helpers to turn the adventure components a client receives into something tests can assert on. */
object Text {
    private val plain = PlainTextComponentSerializer.plainText()
    private val gson = GsonComponentSerializer.gson()

    fun plain(component: Component?): String = component?.let { plain.serialize(it) } ?: ""

    fun json(component: Component?): String = component?.let { gson.serialize(it) } ?: ""

    /** Every node of the component tree, depth first. */
    fun flatten(component: Component): List<Component> {
        val out = ArrayList<Component>()
        fun walk(c: Component) {
            out += c
            c.children().forEach(::walk)
        }
        walk(component)
        return out
    }

    /**
     * Click events in the component with the plain text of the node that carries them (including its
     * children), e.g. ("[✔]", run_command "/friend accept DevPlayer1").
     */
    fun clicks(component: Component): List<Click> {
        val out = ArrayList<Click>()
        fun walk(c: Component, inherited: ClickEvent<*>?) {
            val own = c.clickEvent()
            val effective = own ?: inherited
            if (own != null) out += Click(plain(c), own.action().toString(), payloadString(own), own)
            c.children().forEach { walk(it, effective) }
        }
        walk(component, null)
        return out
    }

    /** Hover texts (show_text) with the plain text of the node they are attached to. */
    fun hovers(component: Component): List<Pair<String, String>> =
        flatten(component).mapNotNull { node ->
            val hover = node.hoverEvent() ?: return@mapNotNull null
            if (hover.action() != HoverEvent.Action.SHOW_TEXT) return@mapNotNull null
            plain(node) to plain(hover.value() as Component)
        }

    fun payloadString(event: ClickEvent<*>): String {
        val payload = event.payload()
        return when (payload) {
            is ClickEvent.Payload.Text -> payload.value()
            else -> payload.toString()
        }
    }

    /**
     * The leading prefix of a surf message: the plain text of the leading nodes up to and including
     * the first one that ends in whitespace or a separator glyph, e.g. ">> " or "⚡ ".
     */
    fun prefix(component: Component): String {
        val texts = flatten(component).filterIsInstance<TextComponent>().map { it.content() }.filter { it.isNotEmpty() }
        val sb = StringBuilder()
        for (t in texts) {
            sb.append(t)
            if (sb.length >= 1 && (t.endsWith(" ") || sb.length > 6)) break
        }
        return sb.toString().take(12)
    }

    private val smallCaps = mapOf(
        'ᴀ' to 'a', 'ʙ' to 'b', 'ᴄ' to 'c', 'ᴅ' to 'd', 'ᴇ' to 'e', 'ғ' to 'f', 'ꜰ' to 'f', 'ɢ' to 'g', 'ʜ' to 'h',
        'ɪ' to 'i', 'ᴊ' to 'j', 'ᴋ' to 'k', 'ʟ' to 'l', 'ᴍ' to 'm', 'ɴ' to 'n', 'ᴏ' to 'o', 'ᴘ' to 'p', 'ǫ' to 'q',
        'ʀ' to 'r', 'ѕ' to 's', 'ꜱ' to 's', 'ᴛ' to 't', 'ᴜ' to 'u', 'ᴠ' to 'v', 'ᴡ' to 'w', 'ʏ' to 'y', 'ᴢ' to 'z',
    )

    /** Lower case with small-caps letters (used in headers and buttons) mapped to ASCII, for matching. */
    fun norm(s: String): String = s.map { smallCaps[it] ?: it }.joinToString("").lowercase()

    /** [text] contains [part], exactly or ignoring case and small caps. */
    fun has(text: String, part: String): Boolean =
        text.contains(part) || (text.any { it in smallCaps } && norm(text).contains(norm(part)))

    /** Colour (hex or name) of the leading "»" prefix glyph of a surf message, inherited colours included. */
    fun prefixColor(component: Component): String? {
        fun walk(c: Component, inherited: String?): String? {
            val color = c.color()?.let { it.asHexString().uppercase() } ?: inherited
            if (c is TextComponent && c.content().trimStart().startsWith("»")) return color
            for (child in c.children()) walk(child, color)?.let { return it }
            return null
        }
        return walk(component, null)
    }

    data class Click(val text: String, val action: String, val value: String, val event: ClickEvent<*>)
}
