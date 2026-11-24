package dev.slne.surf.player.core.client.friends.connection

import dev.slne.surf.player.core.client.vanish.CommandViewer
import dev.slne.surf.player.core.client.vanish.permission.VanishPermissions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class FriendGreetingCountTest {
    private val visibleFriend = UUID.randomUUID()
    private val vanishedFriend = UUID.randomUUID()
    private val friends = listOf(visibleFriend, vanishedFriend)
    private val priorities = mapOf(vanishedFriend to 0)

    private fun viewer(vararg permissions: String) =
        CommandViewer(UUID.randomUUID(), { it in permissions }, { permissions.asSequence() })

    @Test
    fun `a vanished friend is not counted for a normal player`() {
        assertEquals(1, FriendConnectionNotifier.visibleFriendCount(viewer(), friends) { priorities[it] })
    }

    @Test
    fun `a vanished friend is counted for viewers who may see them`() {
        assertEquals(2, FriendConnectionNotifier.visibleFriendCount(viewer(VanishPermissions.VANISH_BYPASS), friends) { priorities[it] })
        assertEquals(2, FriendConnectionNotifier.visibleFriendCount(viewer(VanishPermissions.TEAM_VANISH), friends) { priorities[it] })
    }

    @Test
    fun `a friend vanished with a higher priority than the viewer's is not counted`() {
        val viewer = viewer("${VanishPermissions.VANISH_PRIORITY}.1")
        assertEquals(1, FriendConnectionNotifier.visibleFriendCount(viewer, friends) { if (it == vanishedFriend) 5 else null })
    }
}
