package dev.slne.surf.social.core.client.settings

import dev.slne.surf.rabbitmq.api.ClientRabbitMQApi
import dev.slne.surf.social.core.common.settings.SettingsInstance

interface ClientSettingsInstance : SettingsInstance {
    val clientLoader: ClientLoader

    override val rabbitApi: ClientRabbitMQApi get() = clientLoader.rabbitApi

    companion object : ClientSettingsInstance by SettingsInstance.INSTANCE as ClientSettingsInstance {
        val INSTANCE get() = SettingsInstance.INSTANCE
    }
}
