package dev.slne.surf.social.microservice.clan

import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.social.core.common.clan.rpc.ClanInviteRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanMemberRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanPlayerRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanRpcService
import dev.slne.surf.social.microservice.SocialMicroserviceModule
import dev.slne.surf.social.microservice.SocialSchemas
import dev.slne.surf.social.microservice.clan.db.migration.migrateLegacyClanUuidColumns
import dev.slne.surf.social.microservice.clan.db.table.ClanInvitesTable
import dev.slne.surf.social.microservice.clan.db.table.ClanMembersTable
import dev.slne.surf.social.microservice.clan.db.table.ClanPlayerTable
import dev.slne.surf.social.microservice.clan.db.table.ClansTable
import dev.slne.surf.social.microservice.clan.rpc.ClanInviteRpcServiceImpl
import dev.slne.surf.social.microservice.clan.rpc.ClanMemberRpcServiceImpl
import dev.slne.surf.social.microservice.clan.rpc.ClanPlayerRpcServiceImpl
import dev.slne.surf.social.microservice.clan.rpc.ClanRpcServiceImpl
import dev.slne.surf.social.microservice.createSchemaWithTables

object ClanMicroserviceModule : SocialMicroserviceModule {
    override suspend fun createTables() {
        createSchemaWithTables(
            SocialSchemas.clan,
            ClansTable,
            ClanMembersTable,
            ClanPlayerTable,
            ClanInvitesTable
        )
        migrateLegacyClanUuidColumns()
    }

    override fun registerHandlers(rabbit: ServerRabbitMQApi) {
        rabbit.registerRpcService<ClanRpcService>(ClanRpcServiceImpl)
        rabbit.registerRpcService<ClanInviteRpcService>(ClanInviteRpcServiceImpl)
        rabbit.registerRpcService<ClanMemberRpcService>(ClanMemberRpcServiceImpl)
        rabbit.registerRpcService<ClanPlayerRpcService>(ClanPlayerRpcServiceImpl)
    }
}
