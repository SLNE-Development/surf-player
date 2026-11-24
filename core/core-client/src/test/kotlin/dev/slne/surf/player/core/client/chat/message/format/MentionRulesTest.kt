package dev.slne.surf.player.core.client.chat.message.format

import dev.slne.surf.player.core.client.vanish.CommandViewer
import dev.slne.surf.player.core.client.vanish.PlayerVisibility
import dev.slne.surf.player.core.client.vanish.permission.VanishPermissions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class MentionRulesTest {
    private val admin = UUID.randomUUID()
    private val player1 = UUID.randomUUID()
    private val player2 = UUID.randomUUID()
    private val moderator = UUID.randomUUID()

    /** DevAdmin is vanished with priority 2. */
    private var vanished = mapOf(admin to 2)
    private val visibility = PlayerVisibility { vanished[it] }

    private fun normal(uuid: UUID) = CommandViewer(uuid, { false })
    private fun staff(uuid: UUID, priority: Int) =
        CommandViewer(uuid, { it == VanishPermissions.TEAM_VANISH }) {
            sequenceOf("${VanishPermissions.VANISH_PRIORITY}.$priority")
        }

    private fun decide(sender: CommandViewer, viewer: CommandViewer, mentioned: UUID) =
        MentionRules.decide(visibility, sender, viewer, mentioned)

    @Test
    fun `a vanished player is plain text for viewers who cannot see them`() {
        assertEquals(
            MentionDecision(highlight = false, ping = false),
            decide(normal(player1), normal(player2), admin)
        )
        // Also for the sender who typed the name.
        assertEquals(
            MentionDecision(highlight = false, ping = false),
            decide(normal(player1), normal(player1), admin)
        )
    }

    @Test
    fun `a vanished player is not pinged by a sender who cannot see them`() {
        assertEquals(
            MentionDecision(highlight = true, ping = false),
            decide(normal(player1), staff(admin, 2), admin)
        )
    }

    @Test
    fun `a vanished player is pinged by a sender who can see them`() {
        assertEquals(
            MentionDecision(highlight = true, ping = true),
            decide(staff(moderator, 2), staff(admin, 2), admin)
        )
    }

    @Test
    fun `staff with a lower priority do not see the mention highlighted`() {
        assertEquals(
            MentionDecision(highlight = false, ping = false),
            decide(normal(player1), staff(moderator, 1), admin)
        )
        assertEquals(
            MentionDecision(highlight = true, ping = false),
            decide(normal(player1), staff(moderator, 2), admin)
        )
    }

    @Test
    fun `a visible player is highlighted for everybody and pinged`() {
        vanished = emptyMap()

        assertEquals(MentionDecision(highlight = true, ping = false), decide(normal(player1), normal(player2), admin))
        assertEquals(MentionDecision(highlight = true, ping = true), decide(normal(player1), normal(admin), admin))
    }

    @Test
    fun `only the mentioned viewer is pinged`() {
        assertEquals(
            MentionDecision(highlight = true, ping = false),
            MentionRules.decide(player2, player1, viewerCanSeeMentioned = true, senderCanSeeMentioned = true)
        )
    }
}
