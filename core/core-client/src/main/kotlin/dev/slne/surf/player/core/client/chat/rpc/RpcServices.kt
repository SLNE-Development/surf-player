package dev.slne.surf.player.core.client.chat.rpc

import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.common.chat.rabbit.rpc.ChatFunctionalityRpcService
import dev.slne.surf.player.core.common.chat.rabbit.rpc.ChatHistoryRpcService
import dev.slne.surf.player.core.common.chat.rabbit.rpc.ChatIgnoreRpcService
import dev.slne.surf.player.core.common.chat.rabbit.rpc.ModerationService

val moderationService by lazy { SurfPlayerClient.rabbitApi.createRpcService<ModerationService>() }
val chatHistoryRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<ChatHistoryRpcService>() }
val chatFunctionalityRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<ChatFunctionalityRpcService>() }
val chatIgnoreRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<ChatIgnoreRpcService>() }
