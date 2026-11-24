package dev.slne.surf.social.api.clan.player

import dev.slne.surf.social.api.clan.util.InternalClanApi
import dev.slne.surf.api.core.util.requiredService
import java.util.*

private val service = requiredService<ClanPlayerService>()

@InternalClanApi
interface ClanPlayerService {
    suspend fun findByUuid(uuid: UUID): ClanPlayer

    companion object : ClanPlayerService by service {
        val INSTANCE get() = service
    }
}