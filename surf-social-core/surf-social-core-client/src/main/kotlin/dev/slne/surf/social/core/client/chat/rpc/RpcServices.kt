package dev.slne.surf.social.core.client.chat.rpc

import dev.slne.surf.social.core.client.SocialClient
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ChatFunctionalityRpcService
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ChatHistoryRpcService
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ChatIgnoreRpcService
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ModerationService

val moderationService by lazy { SocialClient.rabbitApi.createRpcService<ModerationService>() }
val chatHistoryRpcService by lazy { SocialClient.rabbitApi.createRpcService<ChatHistoryRpcService>() }
val chatFunctionalityRpcService by lazy { SocialClient.rabbitApi.createRpcService<ChatFunctionalityRpcService>() }
val chatIgnoreRpcService by lazy { SocialClient.rabbitApi.createRpcService<ChatIgnoreRpcService>() }
