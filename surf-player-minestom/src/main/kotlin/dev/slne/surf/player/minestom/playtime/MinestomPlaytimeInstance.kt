package dev.slne.surf.player.minestom.playtime

import com.google.auto.service.AutoService
import dev.slne.surf.player.core.client.playtime.AbstractClientPlaytimeInstance
import dev.slne.surf.player.core.common.playtime.PlaytimeInstance

@AutoService(PlaytimeInstance::class)
class MinestomPlaytimeInstance : AbstractClientPlaytimeInstance() {
    override val dataPath get() = PlaytimeMinestomPlugin.dataDirectory
}
