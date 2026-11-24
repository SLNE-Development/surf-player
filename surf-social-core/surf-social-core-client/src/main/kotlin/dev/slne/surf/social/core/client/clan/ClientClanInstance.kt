package dev.slne.surf.social.core.client.clan

import dev.slne.surf.social.core.common.clan.ClanInstance
import dev.slne.surf.social.core.client.clan.rabbit.RabbitService
import dev.slne.surf.social.core.client.clan.redis.RedisService

abstract class ClientClanInstance : ClanInstance {

    override suspend fun load() {
        super.load()
        RabbitService.connect()
        RedisService.get().connect()
    }

    override suspend fun disable() {
        super.disable()

        RedisService.get().disconnect()
        RabbitService.disconnect()
    }
}