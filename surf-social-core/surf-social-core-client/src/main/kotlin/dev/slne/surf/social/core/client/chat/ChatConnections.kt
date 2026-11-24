package dev.slne.surf.social.core.client.chat

import dev.slne.surf.social.core.client.SocialClient

/** Chat uses the shared surf-social connections owned by [SocialClient]. */
val redisApi get() = SocialClient.redisApi
val rabbitApi get() = SocialClient.rabbitApi
