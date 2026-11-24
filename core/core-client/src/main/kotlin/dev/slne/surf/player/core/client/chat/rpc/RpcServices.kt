package dev.slne.surf.player.core.client.chat.rpc

import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.common.chat.rpc.ChatFunctionalityRpcService
import dev.slne.surf.player.core.common.chat.rpc.ChatHistoryRpcService
import dev.slne.surf.player.core.common.chat.rpc.ChatIgnoreRpcService
import dev.slne.surf.player.core.common.chat.rpc.ChatModerationRpcService

val chatModerationRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<ChatModerationRpcService>() }
val chatHistoryRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<ChatHistoryRpcService>() }
val chatFunctionalityRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<ChatFunctionalityRpcService>() }
val chatIgnoreRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<ChatIgnoreRpcService>() }
