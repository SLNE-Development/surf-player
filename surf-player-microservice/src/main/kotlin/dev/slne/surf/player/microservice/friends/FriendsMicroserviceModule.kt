package dev.slne.surf.social.microservice.friends

import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.social.core.common.friends.rpc.FriendsRpcService
import dev.slne.surf.social.microservice.SocialMicroserviceModule
import dev.slne.surf.social.microservice.SocialSchemas
import dev.slne.surf.social.microservice.createSchemaWithTables
import dev.slne.surf.social.microservice.friends.config.FriendsConfig
import dev.slne.surf.social.microservice.friends.cooldown.FriendRequestCooldowns
import dev.slne.surf.social.microservice.friends.db.table.FriendRequestCooldownsTable
import dev.slne.surf.social.microservice.friends.db.table.FriendRequestsTable
import dev.slne.surf.social.microservice.friends.db.table.FriendshipsTable
import dev.slne.surf.social.microservice.friends.rpc.FriendsRpcServiceImpl
import java.nio.file.Path

object FriendsMicroserviceModule : SocialMicroserviceModule {
    override fun loadConfig(dataPath: Path) {
        FriendsConfig.load()
    }

    /** Also drops stale cooldowns, so their table does not grow forever. */
    override suspend fun createTables() {
        createSchemaWithTables(
            SocialSchemas.friends,
            FriendRequestsTable,
            FriendshipsTable,
            FriendRequestCooldownsTable
        )
        FriendRequestCooldowns.deleteStale()
    }

    override fun registerRpcServices(rabbit: ServerRabbitMQApi) {
        rabbit.registerRpcService<FriendsRpcService>(FriendsRpcServiceImpl)
    }
}
