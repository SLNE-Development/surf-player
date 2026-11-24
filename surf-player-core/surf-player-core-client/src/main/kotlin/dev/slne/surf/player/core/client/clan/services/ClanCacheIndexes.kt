package dev.slne.surf.player.core.client.clan.services

import dev.slne.surf.redis.cache.RedisSetIndexes
import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import dev.slne.surf.player.core.common.clan.clan.ClanTagRules

/**
 * The indexes clans can be looked up by in the clan cache.
 */
internal object ClanCacheIndexes : RedisSetIndexes<ClanImpl>() {
    val id by indexOne(valueOf = { it.clanID })
    val tag by indexOne(normalize = { ClanTagRules.normalize(it) }, valueOf = { it.tag })
    val uuid by indexOne(valueOf = { it.uuid })
    val members by index(valuesOf = { clan -> clan.members.map { it.uuid } })
}
