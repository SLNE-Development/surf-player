package dev.slne.surf.social.core.common.chat.rabbit.rpc

import dev.slne.surf.rabbitmq.api.rpc.RpcService
import dev.slne.surf.social.api.chat.entry.IgnoreListEntry
import java.util.*

/** The players each player ignores in chat, stored by the microservice. */
@RpcService
interface ChatIgnoreRpcService {
    /** @return `false` if [playerUuid] already ignores [targetUuid] */
    suspend fun ignore(playerUuid: UUID, targetUuid: UUID): Boolean

    /** @return `false` if [playerUuid] did not ignore [targetUuid] */
    suspend fun unignore(playerUuid: UUID, targetUuid: UUID): Boolean

    suspend fun findIgnoreListEntries(playerUuid: UUID): List<IgnoreListEntry>
}
