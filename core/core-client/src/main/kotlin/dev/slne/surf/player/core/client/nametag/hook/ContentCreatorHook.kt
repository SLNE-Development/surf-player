package dev.slne.surf.player.core.client.nametag.hook

import dev.slne.surf.player.api.contentcreator.SurfContentCreatorApi
import dev.slne.surf.player.core.client.nametag.platform.NametagPlatform
import dev.slne.surf.player.core.client.nametag.service.NametagService

object ContentCreatorHook {
    fun registerListener() {
        SurfContentCreatorApi.registerStateChangeListener { playerUuid, _, _ ->
            val player = NametagPlatform.player(playerUuid) ?: return@registerStateChangeListener
            NametagPlatform.launch { NametagService.handleDataUpdate(player) }
        }
    }
}
