package dev.slne.surf.player.microservice

import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import java.nio.file.Path

/**
 * One feature hosted by [PlayerMicroservice].
 *
 * The microservice drives every module through the same lifecycle: [loadConfig] for all modules,
 * then [createTables] for all modules inside one shared transaction, then [registerRpcServices] for
 * all modules, before RabbitMQ connects.
 */
interface PlayerMicroserviceModule {
    /** Loads the module's configuration from [dataPath]. Runs before the database is touched. */
    fun loadConfig(dataPath: Path) = Unit

    /** Creates the module's schema and tables. Runs inside the shared bootstrap transaction. */
    suspend fun createTables()

    /** Registers the module's RPC services. Runs before RabbitMQ connects. */
    fun registerRpcServices(rabbit: ServerRabbitMQApi) = Unit
}
