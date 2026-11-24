package dev.slne.surf.social.core.client.nametag.hook

import dev.slne.surf.social.api.contentcreator.ContentCreatorPlatform
import dev.slne.surf.social.api.contentcreator.listener.StateChangeListener
import dev.slne.surf.social.api.contentcreator.platform.PlatformState
import dev.slne.surf.social.core.client.contentcreator.ContentCreatorInstance
import dev.slne.surf.social.core.client.nametag.platform.NametagPlatform
import dev.slne.surf.social.core.client.nametag.service.NametagService
import java.util.*

object ContentCreatorHook {
    fun registerListener() {
        ContentCreatorInstance.registerStateChangeListener(object : StateChangeListener {
            override fun onStateChanged(
                playerUuid: UUID,
                contentCreatorPlatform: ContentCreatorPlatform,
                newState: PlatformState
            ) {
                val player = NametagPlatform.player(playerUuid) ?: return
                NametagPlatform.launch { NametagService.handleDataUpdate(player) }
            }
        })
    }
}
