package dev.slne.surf.social.api.contentcreator

import dev.slne.surf.social.api.contentcreator.platform.PlatformState
import dev.slne.surf.social.api.contentcreator.platform.PlatformType

/**
 * Represents a content creator plattform
 *
 * @param name The name of the plattform
 * @param plattform The type of the plattform
 * @param state The state of the plattform
 */
data class ContentCreatorPlatform(
    val name: String,
    val plattform: PlatformType,
    @Volatile var state: PlatformState = PlatformState.UNKNOWN
)

fun PlatformType.toPlattform(name: String, state: PlatformState = PlatformState.UNKNOWN): ContentCreatorPlatform =
    ContentCreatorPlatform(
        name = name,
        plattform = this,
        state = state
    )
