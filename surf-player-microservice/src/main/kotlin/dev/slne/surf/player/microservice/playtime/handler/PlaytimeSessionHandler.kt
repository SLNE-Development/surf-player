package dev.slne.surf.social.microservice.playtime.handler

import dev.slne.surf.social.core.common.playtime.rabbit.packet.request.*
import dev.slne.surf.social.core.common.playtime.rabbit.packet.response.ManySessionsResponsePacket
import dev.slne.surf.social.core.common.playtime.rabbit.packet.response.PlaytimePerPlayerResponsePacket
import dev.slne.surf.social.microservice.playtime.database.repository.PlaytimeRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.coroutines.launch

object PlaytimeSessionHandler {
    @RabbitHandler
    fun handleSaveSessionRequest(request: SaveSessionRequestPacket) = request.launch {
        request.respond(
            PrimitiveResponse.BooleanResponsePacket(
                PlaytimeRepository.saveSession(
                    request.session
                )
            )
        )
    }

    @RabbitHandler
    fun handleLoadSessionsByPlayerUuidRequest(request: LoadSessionsByPlayerUuidRequestPacket) =
        request.launch {
            request.respond(ManySessionsResponsePacket(PlaytimeRepository.loadSessions(request.playerUuid)))
        }

    @RabbitHandler
    fun handleLoadSessionsByServerRequest(request: LoadSessionsByPlayerUuidAndServerNameRequestPacket) =
        request.launch {
            request.respond(
                ManySessionsResponsePacket(
                    PlaytimeRepository.loadSessionsByServer(
                        request.playerUuid,
                        request.serverName
                    )
                )
            )
        }

    @RabbitHandler
    fun handleLoadSessionsByCategoryRequest(request: LoadSessionsByPlayerUuidAndServerCategoryRequestPacket) =
        request.launch {
            request.respond(
                ManySessionsResponsePacket(
                    PlaytimeRepository.loadSessionsByCategory(
                        request.playerUuid,
                        request.serverCategory
                    )
                )
            )
        }

    @RabbitHandler
    fun handleLoadSecondsByServerName(request: LoadSecondsByPlayerUuidAndServerNameRequestPacket) =
        request.launch {
            request.respond(
                PrimitiveResponse.LongResponsePacket(
                    PlaytimeRepository.loadPlaytimeSecondsByServer(
                        request.playerUuid,
                        request.serverName
                    )
                )
            )
        }

    @RabbitHandler
    fun handleLoadPlaytimePerPlayerRequest(request: LoadPlaytimePerPlayerRequestPacket) =
        request.launch {
            request.respond(
                PlaytimePerPlayerResponsePacket(
                    PlaytimeRepository.loadPlaytimePerPlayer(request.filter)
                )
            )
        }
}