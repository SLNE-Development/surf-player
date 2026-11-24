package dev.slne.surf.player.core.client.chat.message.format

import dev.slne.surf.player.core.client.vanish.CommandViewer
import dev.slne.surf.player.core.client.vanish.PlayerVisibility
import java.util.*

/**
 * How one viewer's copy of a chat message treats a mentioned player.
 *
 * @property highlight whether the mention is rendered highlighted (and clickable); otherwise it
 * stays plain text
 * @property ping whether the viewer, who is the mentioned player, is pinged
 */
data class MentionDecision(val highlight: Boolean, val ping: Boolean)

/**
 * The one rule for mentions in chat, on Paper and Minestom alike: a mention of a vanished player
 * must not reveal them.
 *
 * - Each viewer sees a mention highlighted only if they may see the mentioned player
 *   ([PlayerVisibility]); for everybody else it is the plain name the sender typed.
 * - The mentioned player is pinged only if the sender may see them: a sender who cannot know that
 *   the player is online cannot ping them.
 */
object MentionRules {

    fun decide(
        viewer: UUID,
        mentioned: UUID,
        viewerCanSeeMentioned: Boolean,
        senderCanSeeMentioned: Boolean,
    ): MentionDecision {
        val highlight = viewerCanSeeMentioned
        return MentionDecision(
            highlight = highlight,
            ping = highlight && viewer == mentioned && senderCanSeeMentioned,
        )
    }

    /** [decide] with the visibility of [sender] and [viewer] read from [visibility]. */
    fun decide(
        visibility: PlayerVisibility,
        sender: CommandViewer,
        viewer: CommandViewer,
        mentioned: UUID,
    ): MentionDecision {
        val viewerUuid = viewer.uuid ?: return MentionDecision(highlight = true, ping = false)
        return decide(
            viewer = viewerUuid,
            mentioned = mentioned,
            viewerCanSeeMentioned = visibility.canSee(viewer, mentioned),
            senderCanSeeMentioned = visibility.canSee(sender, mentioned),
        )
    }
}
