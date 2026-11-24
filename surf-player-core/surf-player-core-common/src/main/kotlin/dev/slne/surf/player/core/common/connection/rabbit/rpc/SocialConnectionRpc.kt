package dev.slne.surf.social.core.common.connection.rabbit.rpc

import dev.slne.surf.rabbitmq.api.rpc.RpcService
import dev.slne.surf.social.api.connection.impl.DiscordConnection
import dev.slne.surf.social.api.connection.impl.TwitchConnection
import java.util.*

@RpcService
interface SocialConnectionRpc {
    /** The Discord account linked to [minecraftUuid], or `null` if there is none. */
    suspend fun findDiscordConnection(minecraftUuid: UUID): DiscordConnection?

    /** The Twitch account linked to [minecraftUuid], or `null` if there is none. */
    suspend fun findTwitchConnection(minecraftUuid: UUID): TwitchConnection?

    suspend fun unlinkMinecraftAccount(minecraftUuid: UUID): UnlinkResult
}
