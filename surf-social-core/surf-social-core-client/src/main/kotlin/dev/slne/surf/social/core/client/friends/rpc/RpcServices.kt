package dev.slne.surf.social.core.client.friends.rpc

import dev.slne.surf.social.core.client.SocialClient
import dev.slne.surf.social.core.common.friends.rpc.FriendsRpcService

val friendsRpcService by lazy { SocialClient.rabbitApi.createRpcService<FriendsRpcService>() }
