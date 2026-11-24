package dev.slne.surf.player.api.contentcreator

import dev.slne.surf.player.api.contentcreator.platform.PlatformState
import dev.slne.surf.player.api.contentcreator.platform.PlatformType

/**
 * Represents a content creator platform
 *
 * @param name The name of the platform
 * @param platform The type of the platform
 * @param state The state of the platform
 */
data class ContentCreatorPlatform(
    val name: String,
    val platform: PlatformType,
    @Volatile var state: PlatformState = PlatformState.UNKNOWN
)

fun PlatformType.toPlatform(name: String, state: PlatformState = PlatformState.UNKNOWN): ContentCreatorPlatform =
    ContentCreatorPlatform(
        name = name,
        platform = this,
        state = state
    )
