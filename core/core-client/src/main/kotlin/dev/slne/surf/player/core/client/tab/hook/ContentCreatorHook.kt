package dev.slne.surf.player.core.client.tab.hook

import dev.slne.surf.player.api.contentcreator.SurfContentCreatorApi
import dev.slne.surf.player.core.client.tab.service.TablistService

object ContentCreatorHook {
    fun registerListener() {
        SurfContentCreatorApi.registerStateChangeListener { playerUuid, _, _ ->
            TablistService.updateEntry(playerUuid)
        }
    }
}
