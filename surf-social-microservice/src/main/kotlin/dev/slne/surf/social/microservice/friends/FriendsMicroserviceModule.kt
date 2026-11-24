package dev.slne.surf.social.microservice.friends

import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.social.core.common.friends.rpc.FriendsRpcService
import dev.slne.surf.social.microservice.SocialMicroserviceModule
import dev.slne.surf.social.microservice.SocialSchemas
import dev.slne.surf.social.microservice.createSchemaWithTables
import dev.slne.surf.social.microservice.friends.db.table.FriendRequestsTable
import dev.slne.surf.social.microservice.friends.db.table.FriendshipsTable
import dev.slne.surf.social.microservice.friends.rpc.FriendsRpcServiceImpl

object FriendsMicroserviceModule : SocialMicroserviceModule {
    override suspend fun createTables() {
        createSchemaWithTables(SocialSchemas.friends, FriendRequestsTable, FriendshipsTable)
    }

    override fun registerRpcServices(rabbit: ServerRabbitMQApi) {
        rabbit.registerRpcService<FriendsRpcService>(FriendsRpcServiceImpl)
    }
}
