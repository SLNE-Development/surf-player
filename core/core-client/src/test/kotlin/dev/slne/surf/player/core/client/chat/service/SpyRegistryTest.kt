package dev.slne.surf.player.core.client.chat.service

import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.api.chat.message.MessageType
import dev.slne.surf.player.core.client.chat.processor.post.PrivateMessageSpyPostChatProcessor
import net.kyori.adventure.text.Component
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*

class SpyRegistryTest {
    private class MemoryStore : SpyStore {
        val entries = HashSet<SpyEntry>()
        override fun snapshot(): Set<SpyEntry> = entries.toSet()
        override fun contains(entry: SpyEntry) = entry in entries
        override fun add(entry: SpyEntry) = entries.add(entry)
        override fun remove(entry: SpyEntry) = entries.remove(entry)
        override fun removeIf(predicate: (SpyEntry) -> Boolean) = entries.removeIf(predicate)
    }

    private val store = MemoryStore()
    private val registry = SpyRegistry(store)

    private val spy = UUID.randomUUID()
    private val otherSpy = UUID.randomUUID()
    private val alice = UUID.randomUUID()
    private val bob = UUID.randomUUID()
    private val carol = UUID.randomUUID()

    @Test
    fun `adding twice is reported`() {
        assertTrue(registry.add(spy, alice))
        assertFalse(registry.add(spy, alice))
        assertTrue(registry.isSpying(spy, alice))
        assertTrue(registry.isSpyingAnyone(spy))
    }

    @Test
    fun `removing an unknown spying is reported`() {
        assertFalse(registry.remove(spy, alice))
        registry.add(spy, alice)
        assertTrue(registry.remove(spy, alice))
        assertFalse(registry.isSpying(spy, alice))
    }

    @Test
    fun `spies see the messages their target sends and receives`() {
        registry.add(spy, alice)

        assertEquals(setOf(spy), registry.spiesOf(alice, bob))
        assertEquals(setOf(spy), registry.spiesOf(bob, alice))
        assertEquals(emptySet<UUID>(), registry.spiesOf(bob, carol))
    }

    @Test
    fun `a spy watching both partners gets the message once`() {
        registry.add(spy, alice)
        registry.add(spy, bob)
        registry.add(otherSpy, bob)

        assertEquals(setOf(spy, otherSpy), registry.spiesOf(alice, bob))
    }

    @Test
    fun `a spy is not shown messages they take part in`() {
        registry.add(spy, alice)

        assertEquals(emptySet<UUID>(), registry.spiesOf(alice, spy))
        assertEquals(emptySet<UUID>(), registry.spiesOf(spy, alice))
    }

    @Test
    fun `clearing only removes the spy's own entries`() {
        registry.add(spy, alice)
        registry.add(spy, bob)
        registry.add(otherSpy, alice)

        assertTrue(registry.clear(spy))
        assertFalse(registry.isSpyingAnyone(spy))
        assertTrue(registry.isSpying(otherSpy, alice))
        assertFalse(registry.clear(spy))
    }

    private fun context(type: MessageType, cancelled: Boolean) = MessageContext(
        MessageData(
            Component.text("hallo"),
            UUID.randomUUID(),
            alice,
            bob,
            OffsetDateTime.now(),
            "lobby",
            null,
            type
        ),
        cancelled,
        mutableSetOf()
    )

    @Test
    fun `only delivered private messages are shown to spies`() {
        assertTrue(PrivateMessageSpyPostChatProcessor.showsToSpies(context(MessageType.DIRECT, cancelled = false)))
        assertFalse(PrivateMessageSpyPostChatProcessor.showsToSpies(context(MessageType.DIRECT, cancelled = true)))
        assertFalse(PrivateMessageSpyPostChatProcessor.showsToSpies(context(MessageType.GLOBAL, cancelled = false)))
    }
}
