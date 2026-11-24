package dev.slne.surf.social.microservice.chat.rpc

import dev.slne.surf.social.api.chat.entry.IgnoreListEntry
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ChatIgnoreRpcService
import dev.slne.surf.social.microservice.chat.repository.ignore.IgnoreRepository
import java.util.*

object ChatIgnoreRpcServiceImpl : ChatIgnoreRpcService {
    override suspend fun ignore(playerUuid: UUID, targetUuid: UUID): Boolean =
        IgnoreRepository.ignore(playerUuid, targetUuid)

    override suspend fun unignore(playerUuid: UUID, targetUuid: UUID): Boolean =
        IgnoreRepository.unignore(playerUuid, targetUuid)

    override suspend fun findIgnoreListEntries(playerUuid: UUID): List<IgnoreListEntry> =
        IgnoreRepository.findAllByUuid(playerUuid)
}
