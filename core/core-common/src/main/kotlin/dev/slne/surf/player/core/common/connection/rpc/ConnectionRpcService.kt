package dev.slne.surf.player.core.common.connection.rpc

import dev.slne.surf.rabbitmq.api.rpc.RpcService
import dev.slne.surf.player.api.connection.impl.DiscordConnection
import dev.slne.surf.player.api.connection.impl.TwitchConnection
import java.util.*

/** The Discord and Twitch accounts linked to Minecraft accounts, stored in `public.account`. */
@RpcService
interface ConnectionRpcService {
    /** The Discord account linked to [minecraftUuid], or `null` if there is none. */
    suspend fun findDiscordConnection(minecraftUuid: UUID): DiscordConnection?

    /** The Twitch account linked to [minecraftUuid], or `null` if there is none. */
    suspend fun findTwitchConnection(minecraftUuid: UUID): TwitchConnection?

    /** Removes the Minecraft account from the linked accounts of its user. */
    suspend fun deleteMinecraftLink(minecraftUuid: UUID): UnlinkResult
}
