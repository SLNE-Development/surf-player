package dev.slne.surf.social.core.client.clan.rpc

import dev.slne.surf.social.core.client.clan.rabbit.rabbitApi
import dev.slne.surf.social.core.common.clan.rpc.ClanInviteRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanMemberRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanPlayerRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanRpcService

val clanRpcService by lazy { rabbitApi.createRpcService<ClanRpcService>() }
val clanInviteRpcService by lazy { rabbitApi.createRpcService<ClanInviteRpcService>() }
val clanMemberRpcService by lazy { rabbitApi.createRpcService<ClanMemberRpcService>() }
val clanPlayerRpcService by lazy { rabbitApi.createRpcService<ClanPlayerRpcService>() }