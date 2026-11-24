package dev.slne.surf.player.core.client.connection.rpc

import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.common.connection.rpc.ConnectionRpcService

val connectionRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<ConnectionRpcService>() }
