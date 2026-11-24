package dev.slne.surf.social.api.contentcreator

import dev.slne.surf.social.api.contentcreator.platform.PlatformType
import java.util.*

interface ContentCreator {

    /**
     * The UUID of the content creator in minecraft
     */
    val minecraftUuid: UUID

    fun getPlatform(type: PlatformType): ContentCreatorPlatform?
}