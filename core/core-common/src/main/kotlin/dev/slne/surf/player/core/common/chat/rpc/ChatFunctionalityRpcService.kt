package dev.slne.surf.player.core.common.chat.rabbit.rpc

import dev.slne.surf.rabbitmq.api.rpc.RpcService
import dev.slne.surf.player.api.chat.functionality.Functionalities

/** The chat functionalities of each server, stored by the microservice. */
@RpcService
interface ChatFunctionalityRpcService {
    /** The functionalities of [serverName]; creates the default ones if the server has none yet. */
    suspend fun findFunctionalities(serverName: String): Functionalities

    /** The functionalities of every known server, by server name. */
    suspend fun findAllFunctionalities(): Map<String, Functionalities>

    suspend fun upsertFunctionalities(serverName: String, functionalities: Functionalities): Boolean
}
