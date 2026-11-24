package dev.slne.surf.social.core.client.contentcreator

import dev.slne.surf.social.api.contentcreator.ContentCreatorApi
import dev.slne.surf.social.api.contentcreator.listener.StateChangeListener
import net.kyori.adventure.text.Component
import java.nio.file.Path
import java.util.*

abstract class CoreContentCreatorApi : ContentCreatorApi {
    abstract val dataPath: Path

    override fun registerStateChangeListener(listener: StateChangeListener) {
        ContentCreatorInstance.registerStateChangeListener(listener)
    }

    override fun renderLiveTag(playerUuid: UUID, space: Boolean): Component =
        LiveTagRenderer.render(playerUuid, space)
}

val coreApi get() = ContentCreatorApi.INSTANCE as CoreContentCreatorApi