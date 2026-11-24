package dev.slne.surf.player.api.contentcreator

import dev.slne.surf.player.api.contentcreator.platform.PlatformState
import dev.slne.surf.player.api.contentcreator.platform.PlatformType

/**
 * A streaming platform account of a content creator, e.g. their Twitch channel.
 *
 * @param name The name of the account on the platform, e.g. the Twitch channel name
 * @param type The streaming platform
 * @param state Whether the content creator is currently live on the platform
 */
data class StreamingPlatform(
    val name: String,
    val type: PlatformType,
    @Volatile var state: PlatformState = PlatformState.UNKNOWN
)

/** The account [name] on this streaming platform. */
fun PlatformType.toStreamingPlatform(name: String, state: PlatformState = PlatformState.UNKNOWN): StreamingPlatform =
    StreamingPlatform(
        name = name,
        type = this,
        state = state
    )
