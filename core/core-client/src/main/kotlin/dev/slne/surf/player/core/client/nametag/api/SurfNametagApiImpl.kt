package dev.slne.surf.player.core.client.nametag.api

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.nametag.SurfNametagApi
import dev.slne.surf.player.api.nametag.nametag.NametagVisibility
import dev.slne.surf.player.core.client.nametag.service.NametagService
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfNametagApi::class)
class SurfNametagApiImpl : SurfNametagApi, Services.Fallback {
    override fun preventNametagUpdates(playerUuid: UUID) {
        NametagService.preventUpdates(playerUuid)
    }

    override fun allowNametagUpdates(playerUuid: UUID) {
        NametagService.allowUpdates(playerUuid)
    }

    override fun setNametagVisibility(playerUuid: UUID, visibility: NametagVisibility) {
        NametagService.overrideVisibility(playerUuid, visibility)
    }

    override fun resetNametagVisibility(playerUuid: UUID) {
        NametagService.resetVisibility(playerUuid)
    }
}
