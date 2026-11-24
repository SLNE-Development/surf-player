package dev.slne.surf.social.core.client.clan

import dev.slne.surf.social.core.common.clan.ClanInstance
import dev.slne.surf.social.core.client.clan.redis.RedisService

/**
 * Connections are shared and owned by [dev.slne.surf.social.core.client.SocialClient];
 * clan only registers its Redis components here, before the shared Redis connection is frozen.
 */
abstract class ClientClanInstance : ClanInstance {

    override suspend fun load() {
        super.load()
        RedisService.get().register()
    }
}
