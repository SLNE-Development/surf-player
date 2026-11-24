package dev.slne.surf.social.core.client.clan.rpc

import dev.slne.surf.social.core.client.SocialClient
import dev.slne.surf.social.core.common.clan.rpc.ClanInviteRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanMemberRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanPlayerRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanRpcService

val clanRpcService by lazy { SocialClient.rabbitApi.createRpcService<ClanRpcService>() }
val clanInviteRpcService by lazy { SocialClient.rabbitApi.createRpcService<ClanInviteRpcService>() }
val clanMemberRpcService by lazy { SocialClient.rabbitApi.createRpcService<ClanMemberRpcService>() }
val clanPlayerRpcService by lazy { SocialClient.rabbitApi.createRpcService<ClanPlayerRpcService>() }
