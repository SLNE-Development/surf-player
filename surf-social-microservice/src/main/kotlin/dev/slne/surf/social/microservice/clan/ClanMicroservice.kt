package dev.slne.surf.social.microservice.clan

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.common.clan.ClanCoreSerializerModule
import dev.slne.surf.social.core.common.clan.ClanInstance
import dev.slne.surf.social.core.common.clan.rpc.ClanInviteRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanMemberRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanPlayerRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanRpcService
import dev.slne.surf.social.microservice.clan.db.migration.migrateLegacyClanUuidColumns
import dev.slne.surf.social.microservice.clan.db.table.ClanInvitesTable
import dev.slne.surf.social.microservice.clan.db.table.ClanMembersTable
import dev.slne.surf.social.microservice.clan.db.table.ClanPlayerTable
import dev.slne.surf.social.microservice.clan.db.table.ClansTable
import dev.slne.surf.social.microservice.clan.handler.clan.*
import dev.slne.surf.social.microservice.clan.handler.invite.*
import dev.slne.surf.social.microservice.clan.handler.member.ChangeClanMemberRoleHandler
import dev.slne.surf.social.microservice.clan.handler.member.CreateClanMemberHandler
import dev.slne.surf.social.microservice.clan.handler.member.DeleteClanMemberHandler
import dev.slne.surf.social.microservice.clan.handler.member.FindClanMemberByUuidHandler
import dev.slne.surf.social.microservice.clan.handler.player.FindClanPlayerByUuidHandler
import dev.slne.surf.social.microservice.clan.handler.player.UpdateClanPlayerAcceptsInvitesHandler
import dev.slne.surf.social.microservice.clan.rpc.ClanInviteRpcServiceImpl
import dev.slne.surf.social.microservice.clan.rpc.ClanMemberRpcServiceImpl
import dev.slne.surf.social.microservice.clan.rpc.ClanPlayerRpcServiceImpl
import dev.slne.surf.social.microservice.clan.rpc.ClanRpcServiceImpl
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import kotlin.io.path.Path

lateinit var clanMicroservice: ClanMicroservice

@AutoService(Microservice::class)
class ClanMicroservice : Microservice() {
    override val dataPath = Path("config")
    val databaseApi = DatabaseApi.create(dataPath)
    val rabbitApi = ServerRabbitMQApi.create("surf-clan", dataPath, ClanCoreSerializerModule.module)

    init {
        clanMicroservice = this
    }

    override suspend fun onBootstrap(args: List<String>) {
        createTables()

        ClanInstance.load()
        ClanInstance.enable()

        // Clan
        rabbitApi.registerRequestHandler(ClanCreateHandler)
        rabbitApi.registerRequestHandler(ClanDeleteHandler)
        rabbitApi.registerRequestHandler(FindAllClansWithoutMembersSortByMemberCountHandler)
        rabbitApi.registerRequestHandler(FindClanByClanIDHandler)
        rabbitApi.registerRequestHandler(FindClanByMemberHandler)
        rabbitApi.registerRequestHandler(FindClanByTagHandler)
        rabbitApi.registerRequestHandler(FindClanByUuidHandler)
        rabbitApi.registerRequestHandler(FindClanTagsByPrefixLimitedHandler)
        rabbitApi.registerRequestHandler(UpdateClanDescriptionHandler)
        rabbitApi.registerRequestHandler(UpdateClanDiscordInviteHandler)
        rabbitApi.registerRequestHandler(UpdateClanTagColorHandler)

        // Invite
        rabbitApi.registerRequestHandler(AcceptClanInviteHandler)
        rabbitApi.registerRequestHandler(CreateClanInviteHandler)
        rabbitApi.registerRequestHandler(DeleteInviteClanInviteHandler)
        rabbitApi.registerRequestHandler(FindPendingInviteByInvitedPlayerAndClanNameHandler)
        rabbitApi.registerRequestHandler(FindPendingInvitesByClanIDHandler)
        rabbitApi.registerRequestHandler(FindPendingInvitesByInvitedHandler)

        // Member
        rabbitApi.registerRequestHandler(ChangeClanMemberRoleHandler)
        rabbitApi.registerRequestHandler(CreateClanMemberHandler)
        rabbitApi.registerRequestHandler(DeleteClanMemberHandler)
        rabbitApi.registerRequestHandler(FindClanMemberByUuidHandler)

        // Player
        rabbitApi.registerRequestHandler(FindClanPlayerByUuidHandler)
        rabbitApi.registerRequestHandler(UpdateClanPlayerAcceptsInvitesHandler)

        rabbitApi.registerRpcService<ClanRpcService>(ClanRpcServiceImpl)
        rabbitApi.registerRpcService<ClanInviteRpcService>(ClanInviteRpcServiceImpl)
        rabbitApi.registerRpcService<ClanMemberRpcService>(ClanMemberRpcServiceImpl)
        rabbitApi.registerRpcService<ClanPlayerRpcService>(ClanPlayerRpcServiceImpl)

        rabbitApi.freezeAndConnect()
    }

    private suspend fun createTables() = suspendTransaction {
        SchemaUtils.create(
            ClansTable,
            ClanMembersTable,
            ClanPlayerTable,
            ClanInvitesTable
        )
        migrateLegacyClanUuidColumns()
    }

    override suspend fun onDisable() {
        ClanInstance.disable()
        rabbitApi.disconnect()
        databaseApi.shutdown()
    }
}
