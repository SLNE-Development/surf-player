package dev.slne.surf.social.core.client.clan.rabbit

import dev.slne.surf.social.core.common.clan.ClanCoreSerializerModule
import dev.slne.surf.social.core.common.clan.ClanInstance
import dev.slne.surf.rabbitmq.api.ClientRabbitMQApi

object RabbitService {
    val rabbitApi = ClientRabbitMQApi.create(
        "surf-clan",
        ClanInstance.dataPath,
        ClanCoreSerializerModule.module
    )

    suspend fun connect() {
        rabbitApi.freezeAndConnect()
    }

    suspend fun disconnect() {
        rabbitApi.disconnect()
    }
}

val rabbitApi get() = RabbitService.rabbitApi