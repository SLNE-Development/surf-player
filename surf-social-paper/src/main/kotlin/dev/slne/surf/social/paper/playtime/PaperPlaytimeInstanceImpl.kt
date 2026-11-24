package dev.slne.surf.social.paper.playtime

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.client.playtime.AbstractClientPlaytimeInstance
import dev.slne.surf.social.core.common.playtime.PlaytimeInstance
import net.kyori.adventure.util.Services

@AutoService(PlaytimeInstance::class)
class PaperPlaytimeInstanceImpl : AbstractClientPlaytimeInstance(), Services.Fallback {
    override val dataPath get() = plugin.dataPath
}
