package dev.slne.surf.player.core.client.chat.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DirectMessageIgnoreTest {

    private fun blocks(
        receiverOnThisServer: Boolean,
        senderExempt: Boolean = false,
        cachedIgnore: Boolean = false,
        storedIgnore: Boolean = false,
    ) = runBlocking {
        IgnoreRules.blocksDirectMessage(
            receiverOnThisServer,
            senderExempt = { senderExempt },
            cachedIgnore = { cachedIgnore },
            storedIgnore = { storedIgnore },
        )
    }

    @Test
    fun `a receiver on this server is checked in the cache`() {
        assertTrue(blocks(receiverOnThisServer = true, cachedIgnore = true))
        assertFalse(blocks(receiverOnThisServer = true, cachedIgnore = false, storedIgnore = true))
    }

    @Test
    fun `a receiver on another server is checked in the stored ignores`() {
        assertTrue(blocks(receiverOnThisServer = false, storedIgnore = true))
        assertFalse(blocks(receiverOnThisServer = false, cachedIgnore = true, storedIgnore = false))
    }

    @Test
    fun `team members are never blocked and nothing is looked up`() {
        val blocked = runBlocking {
            IgnoreRules.blocksDirectMessage(
                receiverOnThisServer = false,
                senderExempt = { true },
                cachedIgnore = { error("must not be asked") },
                storedIgnore = { error("must not be asked") },
            )
        }

        assertFalse(blocked)
    }

    @Test
    fun `the stored ignores are only asked for a receiver on another server`() {
        val blocked = runBlocking {
            IgnoreRules.blocksDirectMessage(
                receiverOnThisServer = true,
                senderExempt = { false },
                cachedIgnore = { false },
                storedIgnore = { error("must not be asked") },
            )
        }

        assertFalse(blocked)
    }
}
