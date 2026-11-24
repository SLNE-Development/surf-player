package dev.slne.surf.social.microservice.clan.db.repository

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.social.core.common.clan.player.ClanPlayerImpl
import java.util.*

private val instance = requiredService<ClanPlayerRepository>()

interface ClanPlayerRepository {
    suspend fun findOrCreateByUuid(uuid: UUID): ClanPlayerImpl
    suspend fun changeAcceptsClanInvites(playerID: ULong, acceptsClanInvites: Boolean): Boolean

    companion object : ClanPlayerRepository by instance {
        val INSTANCE get() = instance
    }
}