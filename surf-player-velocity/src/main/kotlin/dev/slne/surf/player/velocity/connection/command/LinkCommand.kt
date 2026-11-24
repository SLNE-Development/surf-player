package dev.slne.surf.social.velocity.connection.command

import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.velocity.command.executors.anyExecutorSuspend
import dev.slne.surf.api.velocity.command.executors.playerExecutorSuspend
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.velocity.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.social.api.connection.SurfConnectionApi
import dev.slne.surf.social.api.connection.findConnection
import dev.slne.surf.social.api.connection.impl.DiscordConnection
import dev.slne.surf.social.api.connection.impl.TwitchConnection
import dev.slne.surf.social.velocity.connection.config
import dev.slne.surf.social.velocity.connection.httpClient
import dev.slne.surf.social.velocity.connection.permission.SocialPermissions
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.format.TextDecoration
import java.util.*
import kotlin.time.Duration.Companion.seconds

private const val ACCOUNT_LINKING_PAGE = "https://auth.castcrafter.de/account?tabs=accounts"
private const val MINECRAFT_LINK_ENDPOINT = "https://auth.castcrafter.de/api/minecraft-link"

fun linkCommand() = commandTree("link") {
    withPermission(SocialPermissions.COMMAND_LINK)

    playerExecutor { player, _ ->
        player.sendText {
            appendInfoPrefix()
            info("Du kannst deine Konten unter ")
            append {
                variableValue(ACCOUNT_LINKING_PAGE)
                decorate(TextDecoration.UNDERLINED)
                clickOpensUrl(ACCOUNT_LINKING_PAGE)
            }

            info(" verknüpfen.")
        }
    }

    stringArgument("code") {
        playerExecutorSuspend { player, arguments ->
            val code: String by arguments

            if (respondMinecraftAuth(player.uniqueId, code)) {
                player.sendText {
                    appendSuccessPrefix()
                    success("Dein Minecraft Account wurde erfolgreich verifiziert.")
                }
            } else {
                player.sendText {
                    appendErrorPrefix()
                    error("Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.")
                }
            }
        }
    }

    literalArgument("lookup") {
        withPermission(SocialPermissions.COMMAND_LINK_LOOKUP)
        surfOfflinePlayerArgument("target") {
            anyExecutorSuspend { sender, arguments ->
                val target = arguments.awaiting<SurfPlayer?>("target")

                if (target == null) {
                    sender.sendText {
                        appendErrorPrefix()
                        error("Der Spieler wurde nicht gefunden.")
                    }
                    return@anyExecutorSuspend
                }

                val start = System.currentTimeMillis()
                val linkedAccounts = linkedAccountNames(target.uuid)

                sender.sendText {
                    appendInfoPrefix()
                    info("Der Spieler ")

                    if (linkedAccounts.isEmpty()) {
                        error("hat keine Verbindungen.")
                    } else {
                        info("ist mit dem ")
                        linkedAccounts.forEachIndexed { index, account ->
                            if (index > 0) info(" und dem ")
                            variableValue(account)
                        }
                        info(" verbunden.")
                    }

                    success(" (${System.currentTimeMillis() - start}ms)")
                }
            }
        }
    }
}

/**
 * The display names of the Twitch and Discord accounts linked to [playerUuid], Twitch first.
 */
private suspend fun linkedAccountNames(playerUuid: UUID): List<String> = coroutineScope {
    val discord = async { SurfConnectionApi.findConnection<DiscordConnection>(playerUuid) }
    val twitch = async { SurfConnectionApi.findConnection<TwitchConnection>(playerUuid) }

    listOfNotNull(
        twitch.await()?.let { "Twitch Account '${it.twitchName}'" },
        discord.await()?.let { "Discord Account '${it.discordName}'" }
    )
}

private suspend fun respondMinecraftAuth(playerUuid: UUID, code: String): Boolean =
    withTimeoutOrNull(5.seconds) {
        runCatching {
            httpClient.post {
                url(MINECRAFT_LINK_ENDPOINT)
                contentType(ContentType.Application.Json)
                bearerAuth(config.minecraftAuthToken)
                userAgent("surf-social-velocity/LinkCommand")

                setBody(MinecraftLinkRequest(playerUuid.toString(), code))
            }.status.isSuccess()
        }.getOrDefault(false)
    } ?: false

@Serializable
private data class MinecraftLinkRequest(
    val minecraftUuid: String,
    val code: String
)
