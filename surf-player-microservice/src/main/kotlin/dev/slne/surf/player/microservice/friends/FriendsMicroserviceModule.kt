package dev.slne.surf.player.microservice.friends

import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.player.core.common.friends.rpc.FriendsRpcService
import dev.slne.surf.player.microservice.PlayerMicroserviceModule
import dev.slne.surf.player.microservice.PlayerSchemas
import dev.slne.surf.player.microservice.createSchemaWithTables
import dev.slne.surf.player.microservice.friends.config.FriendsConfig
import dev.slne.surf.player.microservice.friends.cooldown.FriendRequestCooldowns
import dev.slne.surf.player.microservice.friends.db.table.FriendRequestCooldownsTable
import dev.slne.surf.player.microservice.friends.db.table.FriendRequestsTable
import dev.slne.surf.player.microservice.friends.db.table.FriendshipsTable
import dev.slne.surf.player.microservice.friends.rpc.FriendsRpcServiceImpl
import java.nio.file.Path

object FriendsMicroserviceModule : PlayerMicroserviceModule {
    override fun loadConfig(dataPath: Path) {
        FriendsConfig.load()
    }

    /** Also drops stale cooldowns, so their table does not grow forever. */
    override suspend fun createTables() {
        createSchemaWithTables(
            PlayerSchemas.friends,
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
