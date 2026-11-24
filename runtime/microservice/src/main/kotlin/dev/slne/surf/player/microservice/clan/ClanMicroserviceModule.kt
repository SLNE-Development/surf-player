package dev.slne.surf.player.microservice.clan

import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.player.core.common.clan.rpc.ClanInviteRpcService
import dev.slne.surf.player.core.common.clan.rpc.ClanMemberRpcService
import dev.slne.surf.player.core.common.clan.rpc.ClanRpcService
import dev.slne.surf.player.microservice.PlayerMicroserviceModule
import dev.slne.surf.player.microservice.PlayerSchemas
import dev.slne.surf.player.microservice.clan.db.migration.migrateLegacyClanUuidColumns
import dev.slne.surf.player.microservice.clan.db.table.ClanInvitesTable
import dev.slne.surf.player.microservice.clan.db.table.ClanMembersTable
import dev.slne.surf.player.microservice.clan.db.table.ClanPlayerTable
import dev.slne.surf.player.microservice.clan.db.table.ClansTable
import dev.slne.surf.player.microservice.clan.rpc.ClanInviteRpcServiceImpl
import dev.slne.surf.player.microservice.clan.rpc.ClanMemberRpcServiceImpl
import dev.slne.surf.player.microservice.clan.rpc.ClanRpcServiceImpl
import dev.slne.surf.player.microservice.createSchemaWithTables

object ClanMicroserviceModule : PlayerMicroserviceModule {
    override suspend fun createTables() {
        createSchemaWithTables(
            PlayerSchemas.clan,
            ClansTable,
            ClanMembersTable,
            ClanPlayerTable,
            ClanInvitesTable
        )
        migrateLegacyClanUuidColumns()
    }

    override fun registerRpcServices(rabbit: ServerRabbitMQApi) {
        rabbit.registerRpcService<ClanRpcService>(ClanRpcServiceImpl)
        rabbit.registerRpcService<ClanInviteRpcService>(ClanInviteRpcServiceImpl)
        rabbit.registerRpcService<ClanMemberRpcService>(ClanMemberRpcServiceImpl)
    }
}
