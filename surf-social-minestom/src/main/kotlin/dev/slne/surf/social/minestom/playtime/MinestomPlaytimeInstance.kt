package dev.slne.surf.social.minestom.playtime

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.client.playtime.AbstractClientPlaytimeInstance
import dev.slne.surf.social.core.common.playtime.PlaytimeInstance

@AutoService(PlaytimeInstance::class)
class MinestomPlaytimeInstance : AbstractClientPlaytimeInstance() {
    override val dataPath get() = PlaytimeMinestomPlugin.dataDirectory
}
