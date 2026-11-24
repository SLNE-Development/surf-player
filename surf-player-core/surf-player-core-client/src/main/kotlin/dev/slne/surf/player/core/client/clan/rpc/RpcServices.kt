package dev.slne.surf.player.core.client.clan.rpc

import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.common.clan.rpc.ClanInviteRpcService
import dev.slne.surf.player.core.common.clan.rpc.ClanMemberRpcService
import dev.slne.surf.player.core.common.clan.rpc.ClanPlayerRpcService
import dev.slne.surf.player.core.common.clan.rpc.ClanRpcService

val clanRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<ClanRpcService>() }
val clanInviteRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<ClanInviteRpcService>() }
val clanMemberRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<ClanMemberRpcService>() }
val clanPlayerRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<ClanPlayerRpcService>() }
