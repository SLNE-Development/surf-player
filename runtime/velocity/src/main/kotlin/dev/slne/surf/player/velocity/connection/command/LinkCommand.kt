package dev.slne.surf.player.velocity.connection.command

import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.api.core.command.args.awaitingOrNull
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.velocity.command.executors.anyExecutorSuspend
import dev.slne.surf.api.velocity.command.executors.playerExecutorSuspend
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.api.connection.SurfConnectionApi
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.connection.permission.ConnectionPermissions
import dev.slne.surf.player.velocity.command.VelocityAdminAction
import dev.slne.surf.player.velocity.command.failCommand
import dev.slne.surf.player.velocity.command.visibleOfflinePlayerArgument
import dev.slne.surf.player.velocity.connection.ConnectionVelocityFeature
import dev.slne.surf.player.velocity.connection.config
import dev.slne.surf.player.velocity.connection.httpClient
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.format.TextDecoration
import java.util.*
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.seconds

private const val ACCOUNT_LINKING_PAGE = "https://auth.castcrafter.de/account?tabs=accounts"
private const val MINECRAFT_LINK_ENDPOINT = "https://auth.castcrafter.de/api/minecraft-link"

/**
 * `/link` explains where accounts are linked, `/link <code>` confirms a link.
 */
fun linkCommand() = commandTree("link") {
    withPermission(ConnectionPermissions.COMMAND_LINK)

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

            if (!respondMinecraftAuth(player.uniqueId, code)) {
                failCommand("Es ist ein Fehler aufgetreten. Bitte versuche es später erneut.")
            }

            player.sendText {
                appendSuccessPrefix()
                success("Dein Minecraft Account wurde erfolgreich verifiziert.")
            }
        }
    }
}

/**
 * The actions of `/surfplayervelocity connection`.
 */
fun connectionAdminActions() = listOf(
    VelocityAdminAction("reload", ConnectionPermissions.ADMIN_RELOAD, "Lädt die Connection-Konfiguration neu") {
        anyExecutor { sender, _ ->
            val millis = measureTimeMillis { ConnectionVelocityFeature.socialConfigManager.reload() }
            sender.sendMessage(CommandMessages.configReloaded("connection", millis))
        }
    },

    VelocityAdminAction(
        "lookup",
        ConnectionPermissions.ADMIN_LOOKUP,
        "Zeigt die verknüpften Twitch- und Discord-Konten eines Spielers",
        "<spieler>"
    ) {
        visibleOfflinePlayerArgument("spieler")
        anyExecutorSuspend { sender, arguments ->
            val target = arguments.awaitingOrNull<SurfPlayer?>("spieler") ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)
            val name = target.lastKnownName ?: target.uuid.toString()

            val start = System.currentTimeMillis()
            val linkedAccounts = linkedAccountNames(target.uuid)

            sender.sendMessage(buildText {
                appendInfoPrefix()
                variableValue(name)

                if (linkedAccounts.isEmpty()) {
                    info(" hat keine verknüpften Konten.")
                } else {
                    info(" ist mit dem ")
                    linkedAccounts.forEachIndexed { index, account ->
                        if (index > 0) info(" und dem ")
                        variableValue(account)
                    }
                    info(" verknüpft.")
                }

                spacer(" (${System.currentTimeMillis() - start}ms)")
            })
        }
    },
)

/**
 * The display names of the Twitch and Discord accounts linked to [playerUuid], Twitch first.
 */
private suspend fun linkedAccountNames(playerUuid: UUID): List<String> = coroutineScope {
    val discord = async { SurfConnectionApi.findDiscordConnection(playerUuid) }
    val twitch = async { SurfConnectionApi.findTwitchConnection(playerUuid) }

    listOfNotNull(
        twitch.await()?.let { "Twitch-Konto '${it.twitchName}'" },
        discord.await()?.let { "Discord-Konto '${it.discordName}'" }
    )
}

private suspend fun respondMinecraftAuth(playerUuid: UUID, code: String): Boolean =
    withTimeoutOrNull(5.seconds) {
        runCatching {
            httpClient.post {
                url(MINECRAFT_LINK_ENDPOINT)
                contentType(ContentType.Application.Json)
                bearerAuth(config.minecraftAuthToken)
                userAgent("surf-player-velocity/LinkCommand")

                setBody(MinecraftLinkRequest(playerUuid.toString(), code))
            }.status.isSuccess()
        }.getOrDefault(false)
    } ?: false

@Serializable
private data class MinecraftLinkRequest(
    val minecraftUuid: String,
    val code: String
)
