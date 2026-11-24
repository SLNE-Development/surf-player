package dev.slne.surf.social.api.clan.player

import dev.slne.surf.social.api.clan.util.InternalClanApi
import dev.slne.surf.api.core.util.requiredService
import java.util.*

private val instance = requiredService<ClanPlayerService>()

@InternalClanApi
interface ClanPlayerService {
    suspend fun findByUuid(uuid: UUID): ClanPlayer

    companion object : ClanPlayerService by instance {
        val INSTANCE get() = instance
    }
}