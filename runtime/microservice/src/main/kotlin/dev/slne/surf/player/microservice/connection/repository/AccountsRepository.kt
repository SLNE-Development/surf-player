package dev.slne.surf.player.microservice.connection.repository

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.Op
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.inSubQuery
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.deleteReturning
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.player.api.connection.impl.DiscordConnection
import dev.slne.surf.player.api.connection.impl.TwitchConnection
import dev.slne.surf.player.core.common.connection.rpc.UnlinkResult
import dev.slne.surf.player.microservice.connection.config.SocialConfig
import dev.slne.surf.player.microservice.connection.table.AccountsTable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

object AccountsRepository {
    private const val PROVIDER_MINECRAFT = "minecraft"
    private const val PROVIDER_DISCORD = "discord"
    private const val PROVIDER_TWITCH = "twitch"
    private val REQUEST_TIMEOUT = Duration.ofSeconds(5)

    private val log = logger()

    private val httpClient = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(REQUEST_TIMEOUT)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    private val discordNameCache = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .asLoadingCache<Long, String> { fetchDiscordName(it) }

    private val twitchNameCache = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .asLoadingCache<Long, String> { fetchTwitchName(it) }

    suspend fun findDiscordConnection(minecraftUuid: UUID): DiscordConnection? {
        val discordId = findLinkedProviderAccountId(PROVIDER_DISCORD, minecraftUuid) ?: return null
        return DiscordConnection(discordId = discordId, discordName = discordNameCache.get(discordId))
    }

    suspend fun findTwitchConnection(minecraftUuid: UUID): TwitchConnection? {
        val twitchId = findLinkedProviderAccountId(PROVIDER_TWITCH, minecraftUuid) ?: return null
        return TwitchConnection(twitchId = twitchId, twitchName = twitchNameCache.get(twitchId))
    }

    /**
     * Removes the Minecraft account link of [minecraftUuid] and reports the Discord account that was
     * linked to the same user, so the caller can update it.
     */
    suspend fun unlinkMinecraftAccount(
        minecraftUuid: UUID
    ): UnlinkResult = suspendTransaction {
        val deletedMinecraftAccount = AccountsTable
            .deleteReturning(listOf(AccountsTable.userId)) {
                (AccountsTable.provider eq PROVIDER_MINECRAFT) and
                        (AccountsTable.providerAccountId eq minecraftUuid.toString())
            }
            .firstOrNull()
            ?: return@suspendTransaction UnlinkResult.NotLinked

        val linkedUserId = deletedMinecraftAccount[AccountsTable.userId]

        val discordId = AccountsTable
            .select(AccountsTable.providerAccountId)
            .where(
                (AccountsTable.provider eq PROVIDER_DISCORD) and
                        (AccountsTable.userId eq linkedUserId)
            )
            .limit(1)
            .firstOrNull()
            ?.getOrNull(AccountsTable.providerAccountId)
            ?.toLongOrNull()

        UnlinkResult.Unlinked(discordId)
    }

    /**
     * Resolves the account id that [provider] has linked to [minecraftUuid], in a single round trip.
     */
    private suspend fun findLinkedProviderAccountId(
        provider: String,
        minecraftUuid: UUID
    ): Long? = suspendTransaction {
        AccountsTable
            .select(AccountsTable.providerAccountId)
            .where(linkedAccountCondition(provider, minecraftUuid))
            .limit(1)
            .firstOrNull()
            ?.getOrNull(AccountsTable.providerAccountId)
            ?.toLongOrNull()
    }

    private fun linkedAccountCondition(provider: String, minecraftUuid: UUID): Op<Boolean> =
        (AccountsTable.provider eq provider) and
                AccountsTable.userId.inSubQuery(
                    AccountsTable
                        .select(AccountsTable.userId)
                        .where(
                            (AccountsTable.provider eq PROVIDER_MINECRAFT) and
                                    (AccountsTable.providerAccountId eq minecraftUuid.toString())
                        )
                )

    private suspend fun fetchDiscordName(discordId: Long): String =
        fetchDisplayName("Discord", discordId, "https://discord.com/api/users/$discordId", {
            header("Authorization", "Bot ${SocialConfig.discordBotToken}")
        }) { body ->
            val user = body.jsonObject
            user.stringOrNull("global_name") ?: user.stringOrNull("username")
        }

    private suspend fun fetchTwitchName(twitchId: Long): String =
        fetchDisplayName("Twitch", twitchId, "https://api.ivr.fi/v2/twitch/user?id=$twitchId") { body ->
            val user = (body as? JsonArray)?.firstOrNull()?.jsonObject
            user?.stringOrNull("displayName") ?: user?.stringOrNull("login")
        }

    /**
     * Fetches the display name of account [id] on [platform] from [url]. Falls back to the id itself
     * when the request fails or the response carries no name.
     */
    private suspend fun fetchDisplayName(
        platform: String,
        id: Long,
        url: String,
        configureRequest: HttpRequest.Builder.() -> Unit = {},
        parseName: (JsonElement) -> String?
    ): String {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .apply(configureRequest)
                .build()

            val response = httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .await()

            if (response.statusCode() != 200) {
                log.atWarning().log("%s API returned status %s for user %s", platform, response.statusCode(), id)
                return id.toString()
            }

            parseName(json.parseToJsonElement(response.body())) ?: id.toString()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            log.atWarning().log("Failed to fetch %s name for id %s: %s", platform, id, e.message)
            id.toString()
        }
    }

    private fun JsonObject.stringOrNull(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull
}
