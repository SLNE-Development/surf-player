package dev.slne.surf.player.core.client.contentcreator.api

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.contentcreator.ContentCreator
import dev.slne.surf.player.api.contentcreator.SurfContentCreatorApi
import dev.slne.surf.player.api.contentcreator.listener.StateChangeListener
import dev.slne.surf.player.core.client.contentcreator.LiveTagRenderer
import dev.slne.surf.player.core.client.contentcreator.service.ContentCreatorService
import net.kyori.adventure.text.Component
import java.util.*

@AutoService(SurfContentCreatorApi::class)
class SurfContentCreatorApiImpl : SurfContentCreatorApi {

    override fun registerStateChangeListener(listener: StateChangeListener) =
        ContentCreatorService.registerStateChangeListener(listener)

    override fun contentCreator(playerUuid: UUID): ContentCreator? =
        ContentCreatorService.getContentCreator(playerUuid)

    override fun isLive(playerUuid: UUID): Boolean = LiveTagRenderer.isLive(playerUuid)

    override fun liveTag(playerUuid: UUID, space: Boolean): Component =
        LiveTagRenderer.render(playerUuid, space)
}
