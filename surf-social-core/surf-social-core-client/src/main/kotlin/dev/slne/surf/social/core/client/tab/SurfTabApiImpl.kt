package dev.slne.surf.social.core.client.tab

import com.google.auto.service.AutoService
import dev.slne.surf.social.api.tab.SurfTabApi
import dev.slne.surf.social.api.tab.placeholder.TabPlaceholder
import dev.slne.surf.social.core.client.tab.service.TablistService
import java.util.*

@AutoService(SurfTabApi::class)
class SurfTabApiImpl : SurfTabApi {

    override fun registerPlaceholder(placeholder: TabPlaceholder) =
        TablistService.registerPlaceholder(placeholder)

    override fun unregisterPlaceholder(placeholder: TabPlaceholder) =
        TablistService.unregisterPlaceholder(placeholder)

    override fun invalidate(vararg placeholders: TabPlaceholder) =
        TablistService.invalidate(placeholders.toSet())

    override fun updateEntry(uuid: UUID) = TablistService.updateEntry(uuid)

    override fun broadcastEntryUpdate(uuid: UUID) = TablistService.broadcastEntryUpdate(uuid)
}
