package dev.slne.surf.player.core.client.playtime.rpc

import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.common.playtime.rpc.PlaytimeRpcService

val playtimeRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<PlaytimeRpcService>() }
