package dev.slne.surf.player.core.client.minimap.tracker

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrackerRelationsTest {
    private val player = UUID.randomUUID()
    private val friend = UUID.randomUUID()
    private val clanMate = UUID.randomUUID()

    private fun relations(vararg sources: RelationSource, previous: TrackerRelations? = null) =
        TrackerRelations(sources.toList(), previous) { _, _ -> }

    @Test
    fun `relations are the union of all sources`() = runBlocking {
        val relations = relations(RelationSource { setOf(friend) }, RelationSource { setOf(clanMate) })
        relations.add(player)
        relations.refresh(listOf(player))

        assertTrue(relations.isRelated(player, friend))
        assertTrue(relations.isRelated(player, clanMate))
        assertFalse(relations.isRelated(player, UUID.randomUUID()))
    }

    @Test
    fun `relatedOf unites every source's relations`() = runBlocking {
        val relations = relations(RelationSource { setOf(friend) }, RelationSource { setOf(clanMate) })
        relations.add(player)
        relations.refresh(listOf(player))

        assertEquals(setOf(friend, clanMate), relations.relatedOf(player))
        assertEquals(emptySet(), relations.relatedOf(UUID.randomUUID()))
    }

    @Test
    fun `players are unrelated before the first refresh`() {
        val relations = relations(RelationSource { setOf(friend) })
        relations.add(player)
        assertFalse(relations.isRelated(player, friend))
    }

    @Test
    fun `failing source keeps its last result`() = runBlocking {
        var fail = false
        var clanMembers = setOf(clanMate)
        val relations = relations(
            RelationSource { if (fail) error("friends service down") else setOf(friend) },
            RelationSource { clanMembers },
        )
        relations.add(player)
        relations.refresh(listOf(player))
        fail = true
        clanMembers = emptySet()
        relations.refresh(listOf(player))

        assertTrue(relations.isRelated(player, friend))
        assertFalse(relations.isRelated(player, clanMate))
    }

    @Test
    fun `source cancelling its own lookup counts as a failure`() = runBlocking {
        var timeout = false
        var clanMembers = setOf(clanMate)
        val relations = relations(
            RelationSource { if (timeout) throw CancellationException("timed out") else setOf(friend) },
            RelationSource { clanMembers },
        )
        relations.add(player)
        relations.refresh(listOf(player))
        timeout = true
        clanMembers = emptySet()
        relations.refresh(listOf(player))

        assertTrue(relations.isRelated(player, friend))
        assertFalse(relations.isRelated(player, clanMate))
    }

    @Test
    fun `refresh stops when its own coroutine is cancelled`() = runBlocking {
        val lookupStarted = CompletableDeferred<Unit>()
        var laterLookups = 0
        val relations = relations(
            RelationSource {
                lookupStarted.complete(Unit)
                awaitCancellation()
            },
            RelationSource {
                laterLookups++
                emptySet()
            },
        )
        relations.add(player)

        val refresh = launch { relations.refresh(listOf(player)) }
        lookupStarted.await()
        refresh.cancelAndJoin()

        assertTrue(refresh.isCancelled)
        assertEquals(0, laterLookups)
    }

    @Test
    fun `source missing a method of the other plugin keeps its last result`() = runBlocking {
        var broken = false
        val relations = relations(RelationSource { if (broken) throw NoSuchMethodError("friendUuid") else setOf(friend) })
        relations.add(player)
        relations.refresh(listOf(player))
        broken = true
        relations.refresh(listOf(player))

        assertTrue(relations.isRelated(player, friend))
    }

    @Test
    fun `failing source warns once per refresh`() = runBlocking {
        val warnings = mutableListOf<String>()
        val relations = TrackerRelations(listOf(RelationSource { error("clan service down") })) { message, _ ->
            warnings += message
        }
        val players = List(3) { UUID.randomUUID() }
        players.forEach(relations::add)
        relations.refresh(players)

        assertEquals(1, warnings.size)
        assertTrue("3" in warnings.single(), warnings.single())
    }

    @Test
    fun `forget drops the player's relations`() = runBlocking {
        val relations = relations(RelationSource { setOf(friend) })
        relations.add(player)
        relations.refresh(listOf(player))
        relations.forget(player)
        assertFalse(relations.isRelated(player, friend))
    }

    @Test
    fun `player forgotten during their lookup stays forgotten`() = runBlocking {
        val lookupStarted = CompletableDeferred<Unit>()
        val answer = CompletableDeferred<Set<UUID>>()
        val relations = relations(RelationSource {
            lookupStarted.complete(Unit)
            answer.await()
        })
        relations.add(player)

        val refresh = launch { relations.refresh(listOf(player)) }
        lookupStarted.await()
        relations.forget(player)
        answer.complete(setOf(friend))
        refresh.join()

        assertFalse(relations.isRelated(player, friend))
    }

    @Test
    fun `source kept across a reconfigure keeps its relations`() = runBlocking {
        val friends = RelationSource { setOf(friend) }
        val old = relations(friends)
        old.add(player)
        old.refresh(listOf(player))

        val reconfigured = relations(friends, RelationSource { setOf(clanMate) }, previous = old)

        assertTrue(reconfigured.isRelated(player, friend))
        assertFalse(reconfigured.isRelated(player, clanMate))
    }
}
