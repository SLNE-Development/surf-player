package dev.slne.surf.social.core.client.clan.rabbit

import dev.slne.surf.rabbitmq.api.ClientRabbitMQApi
import dev.slne.surf.social.core.client.SocialClient

/** Clan uses the shared surf-social RabbitMQ connection owned by [SocialClient]. */
object RabbitService {
    val rabbitApi: ClientRabbitMQApi get() = SocialClient.rabbitApi
}

val rabbitApi get() = RabbitService.rabbitApi
