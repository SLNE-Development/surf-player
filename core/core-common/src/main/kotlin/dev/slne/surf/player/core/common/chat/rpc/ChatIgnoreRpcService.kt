package dev.slne.surf.player.core.common.chat.rpc

import dev.slne.surf.rabbitmq.api.rpc.RpcService
import dev.slne.surf.player.api.chat.entry.IgnoreListEntry
import java.util.*

/** The players each player ignores in chat, stored by the microservice. */
@RpcService
interface ChatIgnoreRpcService {
    /**
     * Stores that [playerUuid] ignores [targetUuid]. Also deletes the pending friend requests
     * between both players, in both directions; the caller removes them from the synchronized
     * friend request list.
     *
     * @return `false` if [playerUuid] already ignores [targetUuid]
     */
    suspend fun createIgnore(playerUuid: UUID, targetUuid: UUID): Boolean

    /** @return `false` if [playerUuid] did not ignore [targetUuid] */
    suspend fun deleteIgnore(playerUuid: UUID, targetUuid: UUID): Boolean

    /**
     * Whether [playerUuid] ignores [targetUuid]; for direct messages to a receiver on another
     * server, whose ignore list the sender's server has not cached.
     */
    suspend fun isIgnoring(playerUuid: UUID, targetUuid: UUID): Boolean

    /** The players [playerUuid] ignores. */
    suspend fun findIgnoreList(playerUuid: UUID): List<IgnoreListEntry>
}
