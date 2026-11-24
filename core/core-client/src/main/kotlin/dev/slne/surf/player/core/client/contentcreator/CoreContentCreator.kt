package dev.slne.surf.player.core.client.contentcreator

import dev.slne.surf.player.api.contentcreator.ContentCreator
import dev.slne.surf.player.api.contentcreator.ContentCreatorPlatform
import dev.slne.surf.player.api.contentcreator.platform.PlatformType
import dev.slne.surf.player.api.contentcreator.toPlatform
import java.util.*

data class CoreContentCreator(
    override val minecraftUuid: UUID,
) : ContentCreator {

    @Volatile
    private var twitchPlatform: ContentCreatorPlatform? = null

    @Volatile
    var twitchName: String? = null
        set(value) {
            twitchPlatform = value?.let { PlatformType.TWITCH.toPlatform(it.lowercase()) }
            field = value
        }

    override fun getPlatform(type: PlatformType): ContentCreatorPlatform? = when (type) {
        PlatformType.TWITCH -> twitchPlatform
    }

    override fun toString(): String {
        return "CoreContentCreator(minecraftUuid=$minecraftUuid, twitchName=$twitchName)"
    }
}
