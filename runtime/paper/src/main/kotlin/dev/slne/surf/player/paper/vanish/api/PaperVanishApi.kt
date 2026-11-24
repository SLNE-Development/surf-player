package dev.slne.surf.player.paper.vanish.api

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.toObjectSet
import dev.slne.surf.player.api.vanish.SurfVanishApi
import dev.slne.surf.player.paper.vanish.service.VanishService
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfVanishApi::class)
class PaperVanishApi : SurfVanishApi, Services.Fallback {
    override fun isVanished(uuid: UUID) = VanishService.isVanished(uuid)
    override fun vanishedPlayers() = VanishService.all()
    override fun onlineVanishedPlayers() =
        VanishService.allOnline().map { it.uniqueId }.toObjectSet()
}
