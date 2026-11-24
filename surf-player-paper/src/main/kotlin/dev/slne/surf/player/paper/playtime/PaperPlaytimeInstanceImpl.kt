package dev.slne.surf.player.paper.playtime

import com.google.auto.service.AutoService
import dev.slne.surf.player.core.client.playtime.AbstractClientPlaytimeInstance
import dev.slne.surf.player.core.common.playtime.PlaytimeInstance
import net.kyori.adventure.util.Services

@AutoService(PlaytimeInstance::class)
class PaperPlaytimeInstanceImpl : AbstractClientPlaytimeInstance(), Services.Fallback {
    override val dataPath get() = plugin.dataPath
}
