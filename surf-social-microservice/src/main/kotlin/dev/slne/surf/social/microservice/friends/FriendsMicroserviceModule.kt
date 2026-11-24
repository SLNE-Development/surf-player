package dev.slne.surf.social.microservice.friends

import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.social.microservice.SocialMicroserviceModule
import dev.slne.surf.social.microservice.SocialSchemas
import dev.slne.surf.social.microservice.createSchemaWithTables
import dev.slne.surf.social.microservice.friends.db.table.FriendRequestsTable
import dev.slne.surf.social.microservice.friends.db.table.FriendshipsTable
import dev.slne.surf.social.microservice.friends.rabbit.handler.FriendRequestHandler
import dev.slne.surf.social.microservice.friends.rabbit.handler.FriendshipHandler

object FriendsMicroserviceModule : SocialMicroserviceModule {
    override suspend fun createTables() {
        createSchemaWithTables(SocialSchemas.friends, FriendRequestsTable, FriendshipsTable)
    }

    override fun registerHandlers(rabbit: ServerRabbitMQApi) {
        rabbit.registerRequestHandler(FriendRequestHandler)
        rabbit.registerRequestHandler(FriendshipHandler)
    }
}
