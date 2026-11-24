package dev.slne.surf.social.api.contentcreator

import dev.slne.surf.social.api.contentcreator.platform.PlatformType
import java.util.*

interface ContentCreator {

    /**
     * The UUID of the content creator in minecraft
     */
    val minecraftUuid: UUID
//
//    /**
//     * The youtube channel of the content creator if any
//     */
//    val youtube: ContentCreatorPlattform?
//
//    /**
//     * The twitch channel of the content creator if any
//     */
//    val twitch: ContentCreatorPlattform?

    fun getPlatform(type: PlatformType): ContentCreatorPlatform?
}