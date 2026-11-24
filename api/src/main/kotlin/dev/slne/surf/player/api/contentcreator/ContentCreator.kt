package dev.slne.surf.player.api.contentcreator

import dev.slne.surf.player.api.contentcreator.platform.PlatformType
import java.util.*

interface ContentCreator {

    /**
     * The UUID of the content creator in minecraft
     */
    val minecraftUuid: UUID

    /** The account of this content creator on the streaming platform [type], if they have one. */
    fun getStreamingPlatform(type: PlatformType): StreamingPlatform?
}