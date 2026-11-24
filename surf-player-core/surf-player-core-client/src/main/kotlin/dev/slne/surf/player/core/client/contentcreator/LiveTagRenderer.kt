package dev.slne.surf.social.core.client.contentcreator

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.social.api.contentcreator.platform.PlatformState
import dev.slne.surf.social.api.contentcreator.platform.PlatformType
import dev.slne.surf.social.core.client.contentcreator.config.config
import dev.slne.surf.social.core.client.contentcreator.service.ContentCreatorService
import net.kyori.adventure.text.Component
import java.util.*

/**
 * Renders the tag that marks a content creator as currently streaming.
 */
object LiveTagRenderer {

    /**
     * Renders the live tag for the content creator behind [playerUuid], or an empty component if
     * that player is no known content creator.
     *
     * @param space whether the tag is prefixed with a space
     */
    fun render(playerUuid: UUID, space: Boolean): Component {
        val creator = ContentCreatorService.getContentCreator(playerUuid) ?: return Component.empty()

        val live = PlatformType.entries
            .any { creator.getPlatform(it)?.state == PlatformState.ONLINE }

        if (!live) {
            return Component.empty()
        }

        return renderTag(true, space, miniMessage.deserialize(config.liveTag))
    }

    internal fun renderTag(live: Boolean, space: Boolean, liveTag: Component): Component {
        if (!live) {
            return Component.empty()
        }

        return buildText {
            if (space) {
                appendSpace()
            }
            append(liveTag)
        }
    }
}
