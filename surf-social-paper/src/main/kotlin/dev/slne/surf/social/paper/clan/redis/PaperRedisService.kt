package dev.slne.surf.social.paper.clan.redis

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.client.clan.redis.RedisService

@AutoService(RedisService::class)
class PaperRedisService : RedisService()