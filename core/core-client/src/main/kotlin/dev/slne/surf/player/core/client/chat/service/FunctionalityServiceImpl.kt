package dev.slne.surf.player.core.client.chat.service

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.player.api.chat.functionality.Functionalities
import dev.slne.surf.player.core.client.chat.rpc.chatFunctionalityRpcService
import dev.slne.surf.player.core.common.chat.service.FunctionalityService
import net.kyori.adventure.util.Services

@AutoService(FunctionalityService::class)
class FunctionalityServiceImpl : FunctionalityService, Services.Fallback {
    @Volatile
    private var functionalities = Functionalities.EMPTY

    override suspend fun fetch(localServer: String) {
        functionalities = chatFunctionalityRpcService.findFunctionalities(localServer)
    }

    override fun getFunctionalities(): Functionalities {
        return functionalities
    }

    override suspend fun updateLocalFunctionalities(functionalities: Functionalities) {
        updateFunctionalities(functionalities, SurfCoreApi.getCurrentServerName())
    }

    override suspend fun updateFunctionalities(
        functionalities: Functionalities,
        localServer: String
    ) {
        chatFunctionalityRpcService.upsertFunctionalities(localServer, functionalities)
        if (localServer == SurfCoreApi.getCurrentServerName()) {
            this.functionalities = functionalities
        }
    }

    override suspend fun getFunctionalities(localServer: String): Functionalities {
        return chatFunctionalityRpcService.findFunctionalities(localServer)
    }

    override suspend fun getFunctionalitiesForAllServers(): Map<String, Functionalities> {
        return chatFunctionalityRpcService.findAllFunctionalities()
    }
}
