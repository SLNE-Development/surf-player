package dev.slne.surf.player.core.client.contentcreator

import dev.slne.surf.player.api.contentcreator.ContentCreator
import dev.slne.surf.player.api.contentcreator.StreamingPlatform
import dev.slne.surf.player.api.contentcreator.platform.PlatformType
import dev.slne.surf.player.api.contentcreator.toStreamingPlatform
import java.util.*

data class CoreContentCreator(
    override val minecraftUuid: UUID,
) : ContentCreator {

    @Volatile
    private var twitchPlatform: StreamingPlatform? = null

    @Volatile
    var twitchName: String? = null
        set(value) {
            twitchPlatform = value?.let { PlatformType.TWITCH.toStreamingPlatform(it.lowercase()) }
            field = value
        }

    override fun getStreamingPlatform(type: PlatformType): StreamingPlatform? = when (type) {
        PlatformType.TWITCH -> twitchPlatform
    }

    override fun toString(): String {
        return "CoreContentCreator(minecraftUuid=$minecraftUuid, twitchName=$twitchName)"
    }
}
