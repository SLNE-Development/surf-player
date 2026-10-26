package dev.slne.surf.minimapcompanion.core.tracker

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrackerRelationsTest {
    private val player = UUID.randomUUID()
    private val friend = UUID.randomUUID()
    private val clanMate = UUID.randomUUID()

    @Test
    fun `relations are the union of all sources`() = runBlocking {
        val relations = TrackerRelations(
            listOf(
                RelationSource { setOf(friend) },
                RelationSource { setOf(clanMate) },
            )
        )
        relations.refresh(listOf(player))

        assertTrue(relations.isRelated(player, friend))
        assertTrue(relations.isRelated(player, clanMate))
        assertFalse(relations.isRelated(player, UUID.randomUUID()))
    }

    @Test
    fun `players are unrelated before the first refresh`() {
        val relations = TrackerRelations(listOf(RelationSource { setOf(friend) }))
        assertFalse(relations.isRelated(player, friend))
    }

    @Test
    fun `failing source keeps its last result`() = runBlocking {
        var fail = false
        var clanMembers = setOf(clanMate)
        val relations = TrackerRelations(
            listOf(
                RelationSource { if (fail) error("friends service down") else setOf(friend) },
                RelationSource { clanMembers },
            )
        )
        relations.refresh(listOf(player))
        fail = true
        clanMembers = emptySet()
        relations.refresh(listOf(player))

        assertTrue(relations.isRelated(player, friend))
        assertFalse(relations.isRelated(player, clanMate))
    }

    @Test
    fun `forget drops the player's relations`() = runBlocking {
        val relations = TrackerRelations(listOf(RelationSource { setOf(friend) }))
        relations.refresh(listOf(player))
        relations.forget(player)
        assertFalse(relations.isRelated(player, friend))
    }
}
