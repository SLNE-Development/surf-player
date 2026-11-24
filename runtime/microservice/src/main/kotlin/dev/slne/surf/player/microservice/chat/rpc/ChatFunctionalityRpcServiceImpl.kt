package dev.slne.surf.player.microservice.chat.rpc

import dev.slne.surf.player.api.chat.functionality.Functionalities
import dev.slne.surf.player.core.common.chat.rpc.ChatFunctionalityRpcService
import dev.slne.surf.player.microservice.chat.repository.functionality.FunctionalityRepository

object ChatFunctionalityRpcServiceImpl : ChatFunctionalityRpcService {
    override suspend fun findFunctionalities(serverName: String): Functionalities =
        FunctionalityRepository.findByServerOrCreate(serverName)

    override suspend fun findAllFunctionalities(): Map<String, Functionalities> =
        FunctionalityRepository.findAll()

    override suspend fun saveFunctionalities(serverName: String, functionalities: Functionalities) {
        FunctionalityRepository.updateOrCreate(serverName, functionalities)
    }
}
