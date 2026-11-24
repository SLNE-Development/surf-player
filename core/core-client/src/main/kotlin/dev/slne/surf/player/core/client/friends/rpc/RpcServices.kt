package dev.slne.surf.player.core.client.friends.rpc

import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.common.friends.rpc.FriendsRpcService

val friendsRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<FriendsRpcService>() }
