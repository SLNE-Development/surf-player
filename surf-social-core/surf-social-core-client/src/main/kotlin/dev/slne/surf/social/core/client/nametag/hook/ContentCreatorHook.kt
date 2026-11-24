package dev.slne.surf.social.core.client.nametag.hook

import dev.slne.surf.social.api.contentcreator.ContentCreatorApi
import dev.slne.surf.social.api.contentcreator.ContentCreatorPlatform
import dev.slne.surf.social.api.contentcreator.listener.StateChangeListener
import dev.slne.surf.social.api.contentcreator.platform.PlatformState
import dev.slne.surf.social.core.client.nametag.platform.NametagPlatform
import dev.slne.surf.social.core.client.nametag.service.NametagService
import net.kyori.adventure.text.Component
import java.util.*

object ContentCreatorHook {
    fun renderLiveTag(playerUuid: UUID): Component =
        ContentCreatorApi.renderLiveTag(playerUuid, space = true)

    fun registerListener() {
        ContentCreatorApi.registerStateChangeListener(object : StateChangeListener {
            override fun onStateChanged(
                playerUuid: UUID,
                contentCreatorPlatform: ContentCreatorPlatform,
                newState: PlatformState
            ) {
                NametagPlatform.player(playerUuid)?.let {
                    NametagPlatform.launch {
                        NametagService.handleDataUpdate(it)
                    }
                }
            }
        })
    }
}
