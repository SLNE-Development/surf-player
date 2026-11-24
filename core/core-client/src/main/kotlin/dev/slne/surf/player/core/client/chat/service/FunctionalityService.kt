package dev.slne.surf.player.core.client.chat.service

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.player.api.chat.functionality.Functionalities
import dev.slne.surf.player.core.client.chat.rpc.chatFunctionalityRpcService

/**
 * The chat functionalities (e.g. local chat) enabled per server.
 */
object FunctionalityService {
    @Volatile
    private var functionalities = Functionalities.EMPTY

    /**
     * Fetches the functionalities of [localServer], the server this client runs on.
     */
    suspend fun fetch(localServer: String) {
        functionalities = chatFunctionalityRpcService.findFunctionalities(localServer)
    }

    fun getFunctionalities(): Functionalities {
        return functionalities
    }

    suspend fun updateLocalFunctionalities(functionalities: Functionalities) {
        updateFunctionalities(functionalities, SurfCoreApi.getCurrentServerName())
    }

    suspend fun updateLocalFunctionalities(update: (Functionalities) -> Functionalities) {
        updateLocalFunctionalities(update(getFunctionalities()))
    }

    suspend fun updateFunctionalities(
        functionalities: Functionalities,
        localServer: String
    ) {
        chatFunctionalityRpcService.saveFunctionalities(localServer, functionalities)
        if (localServer == SurfCoreApi.getCurrentServerName()) {
            this.functionalities = functionalities
        }
    }

    suspend fun updateFunctionalities(
        localServer: String,
        update: (Functionalities) -> Functionalities
    ) {
        updateFunctionalities(update(getFunctionalities(localServer)), localServer)
    }

    suspend fun getFunctionalities(localServer: String): Functionalities {
        return chatFunctionalityRpcService.findFunctionalities(localServer)
    }

    suspend fun getFunctionalitiesForAllServers(): Map<String, Functionalities> {
        return chatFunctionalityRpcService.findAllFunctionalities()
    }
}
