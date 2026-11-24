package dev.slne.surf.social.microservice.friends

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.social.microservice.friends.db.table.FriendRequestsTable
import dev.slne.surf.social.microservice.friends.db.table.FriendshipsTable
import dev.slne.surf.social.microservice.friends.rabbit.handler.FriendRequestHandler
import dev.slne.surf.social.microservice.friends.rabbit.handler.FriendshipHandler

object FriendsMicroserviceModule {
    /** Must be called inside a transaction. */
    suspend fun createTables() {
        SchemaUtils.create(
            FriendRequestsTable,
            FriendshipsTable
        )
    }

    fun registerHandlers(rabbit: ServerRabbitMQApi) {
        rabbit.registerRequestHandler(FriendRequestHandler)
        rabbit.registerRequestHandler(FriendshipHandler)
    }
}
