package dev.slne.surf.social.microservice.chat.rpc

import dev.slne.surf.social.api.chat.functionality.Functionalities
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ChatFunctionalityRpcService
import dev.slne.surf.social.microservice.chat.repository.functionality.FunctionalityRepository

object ChatFunctionalityRpcServiceImpl : ChatFunctionalityRpcService {
    override suspend fun findFunctionalities(serverName: String): Functionalities =
        FunctionalityRepository.findByServerOrCreate(serverName)

    override suspend fun findAllFunctionalities(): Map<String, Functionalities> =
        FunctionalityRepository.findAll()

    override suspend fun upsertFunctionalities(serverName: String, functionalities: Functionalities): Boolean =
        FunctionalityRepository.updateOrCreate(serverName, functionalities)
}
