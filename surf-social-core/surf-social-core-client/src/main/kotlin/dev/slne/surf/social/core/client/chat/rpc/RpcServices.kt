package dev.slne.surf.social.core.client.chat.rpc

import dev.slne.surf.social.core.client.SocialClient
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ModerationService

val moderationService by lazy { SocialClient.rabbitApi.createRpcService<ModerationService>() }
