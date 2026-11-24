package dev.slne.surf.social.velocity.clan.redis

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.client.clan.redis.RedisService

@AutoService(RedisService::class)
class VelocityRedisService : RedisService()