package dev.slne.surf.social.microservice.clan

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.social.core.common.clan.ClanInstance
import dev.slne.surf.social.core.common.clan.rpc.ClanInviteRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanMemberRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanPlayerRpcService
import dev.slne.surf.social.core.common.clan.rpc.ClanRpcService
import dev.slne.surf.social.microservice.SocialMicroservice
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

/** The microservice hosting clan; set by [SocialMicroservice] on construction. */
lateinit var clanMicroservice: SocialMicroservice

object ClanMicroserviceModule {
    /** Must be called inside a transaction. */
    suspend fun createTables() {
        SchemaUtils.create(
            ClansTable,
            ClanMembersTable,
            ClanPlayerTable,
            ClanInvitesTable
        )
        migrateLegacyClanUuidColumns()
    }

    suspend fun load() {
        ClanInstance.load()
        ClanInstance.enable()
    }

    suspend fun disable() {
        ClanInstance.disable()
    }

    fun registerHandlers(rabbit: ServerRabbitMQApi) {
        // Clan
        rabbit.registerRequestHandler(ClanCreateHandler)
        rabbit.registerRequestHandler(ClanDeleteHandler)
        rabbit.registerRequestHandler(FindAllClansWithoutMembersSortByMemberCountHandler)
        rabbit.registerRequestHandler(FindClanByClanIDHandler)
        rabbit.registerRequestHandler(FindClanByMemberHandler)
        rabbit.registerRequestHandler(FindClanByTagHandler)
        rabbit.registerRequestHandler(FindClanByUuidHandler)
        rabbit.registerRequestHandler(FindClanTagsByPrefixLimitedHandler)
        rabbit.registerRequestHandler(UpdateClanDescriptionHandler)
        rabbit.registerRequestHandler(UpdateClanDiscordInviteHandler)
        rabbit.registerRequestHandler(UpdateClanTagColorHandler)

        // Invite
        rabbit.registerRequestHandler(AcceptClanInviteHandler)
        rabbit.registerRequestHandler(CreateClanInviteHandler)
        rabbit.registerRequestHandler(DeleteInviteClanInviteHandler)
        rabbit.registerRequestHandler(FindPendingInviteByInvitedPlayerAndClanNameHandler)
        rabbit.registerRequestHandler(FindPendingInvitesByClanIDHandler)
        rabbit.registerRequestHandler(FindPendingInvitesByInvitedHandler)

        // Member
        rabbit.registerRequestHandler(ChangeClanMemberRoleHandler)
        rabbit.registerRequestHandler(CreateClanMemberHandler)
        rabbit.registerRequestHandler(DeleteClanMemberHandler)
        rabbit.registerRequestHandler(FindClanMemberByUuidHandler)

        // Player
        rabbit.registerRequestHandler(FindClanPlayerByUuidHandler)
        rabbit.registerRequestHandler(UpdateClanPlayerAcceptsInvitesHandler)

        rabbit.registerRpcService<ClanRpcService>(ClanRpcServiceImpl)
        rabbit.registerRpcService<ClanInviteRpcService>(ClanInviteRpcServiceImpl)
        rabbit.registerRpcService<ClanMemberRpcService>(ClanMemberRpcServiceImpl)
        rabbit.registerRpcService<ClanPlayerRpcService>(ClanPlayerRpcServiceImpl)
    }
}
