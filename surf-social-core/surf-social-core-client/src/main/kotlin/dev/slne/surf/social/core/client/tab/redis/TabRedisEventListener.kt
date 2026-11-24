package dev.slne.surf.social.core.client.tab.redis

import dev.slne.surf.redis.event.OnRedisEvent
import dev.slne.surf.social.api.tab.redis.TabEntryUpdateRedisEvent
import dev.slne.surf.social.core.client.tab.service.TablistService

object TabRedisEventListener {
    @OnRedisEvent
    fun onUpdate(event: TabEntryUpdateRedisEvent) {
        TablistService.updateEntry(event.toUpdateUuid)
    }
}
