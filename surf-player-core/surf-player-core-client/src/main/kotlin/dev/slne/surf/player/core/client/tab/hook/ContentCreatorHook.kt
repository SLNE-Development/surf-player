package dev.slne.surf.social.core.client.tab.hook

import dev.slne.surf.social.api.contentcreator.ContentCreatorPlatform
import dev.slne.surf.social.api.contentcreator.listener.StateChangeListener
import dev.slne.surf.social.api.contentcreator.platform.PlatformState
import dev.slne.surf.social.core.client.contentcreator.ContentCreatorInstance
import dev.slne.surf.social.core.client.tab.service.TablistService
import java.util.*

object ContentCreatorHook {
    fun registerListener() {
        ContentCreatorInstance.registerStateChangeListener(object : StateChangeListener {
            override fun onStateChanged(
                playerUuid: UUID,
                contentCreatorPlatform: ContentCreatorPlatform,
                newState: PlatformState
            ) {
                TablistService.updateEntry(playerUuid)
            }
        })
    }
}
