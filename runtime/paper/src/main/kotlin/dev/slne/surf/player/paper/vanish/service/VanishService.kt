package dev.slne.surf.player.paper.vanish.service

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.api.core.util.toObjectList
import dev.slne.surf.api.core.util.toObjectSet
import dev.slne.surf.api.paper.glow.SurfGlowingApi
import dev.slne.surf.api.paper.scoreboard.SurfScoreboard
import dev.slne.surf.api.paper.scoreboard.SurfScoreboardApi
import dev.slne.surf.api.paper.util.forEachPlayerInRegion
import dev.slne.surf.player.api.vanish.event.PlayerReappearEvent
import dev.slne.surf.player.api.vanish.event.PlayerVanishEvent
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.util.luckPermsPrefix
import dev.slne.surf.player.paper.plugin
import dev.slne.surf.player.paper.vanish.config.vanishConfig
import dev.slne.surf.player.paper.vanish.util.*
import dev.slne.surf.redis.sync.set.SyncSet
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/** The color a spectating player sees their current target glowing in. */
val VANISH_GLOW_COLOR: NamedTextColor = NamedTextColor.GREEN

object VanishService {
    private lateinit var vanishedPlayers: SyncSet<UUID>
    private var actionbarTask: ScheduledTask? = null

    private val playerQueues = ConcurrentHashMap<UUID, AuditableQueue>()
    private val scoreboards = ConcurrentHashMap<UUID, SurfScoreboard>()
    private val spectateModePlayers = ConcurrentHashMap.newKeySet<UUID>()
    private val playerFlyStates = ConcurrentHashMap<UUID, Boolean>()

    /**
     * Creates the network wide set of vanished players. Must run before
     * [SurfPlayerClient.connectRedis].
     */
    fun registerRedis() {
        vanishedPlayers = SurfPlayerClient.redisApi.createSyncSet<UUID>("surf-vanish:vanished_players_v2")
    }

    fun vanish(player: Player) {
        if (vanishedPlayers.add(player.uniqueId).not()) {
            return
        }

        playerQueues[player.uniqueId] = AuditableQueue()

        player.setMetaVanished(true)

        if (isSpectating(player.uniqueId)) {
            createAndShowScoreboard(player)
        }

        val spoofMessage = spoofedConnectionMessage(player, "<red>-")

        val visibleMessage = buildText {
            appendInfoPrefix()
            variableValue(player.name)
            info(" ist nun unsichtbar.")
        }

        plugin.launch {
            forEachPlayerInRegion(plugin, { onlinePlayer ->
                if (onlinePlayer.uniqueId == player.uniqueId) {
                    return@forEachPlayerInRegion
                }

                if (!onlinePlayer.canVanishSee(player)) {
                    if (onlinePlayer.canSee(player)) {
                        onlinePlayer.hidePlayer(plugin, player)
                    }

                    spoofMessage?.let(onlinePlayer::sendMessage)
                } else {
                    onlinePlayer.sendMessage(visibleMessage)
                }
            }, true)
        }

        PlayerVanishEvent(player.uniqueId).call()
    }

    fun reappear(player: Player) {
        if (vanishedPlayers.remove(player.uniqueId).not()) {
            return
        }

        current(player)?.player?.let { currentPlayer ->
            SurfGlowingApi.removeGlowing(currentPlayer, player)
        }

        player.sendActionBar(Component.empty())
        player.setMetaVanished(false)

        playerQueues.remove(player.uniqueId)

        hideAndDeleteScoreboard(player)

        val spoofMessage = spoofedConnectionMessage(player, "<green>+")

        val visibleMessage = buildText {
            appendInfoPrefix()
            variableValue(player.name)
            info(" ist nun sichtbar.")
        }

        plugin.launch {
            forEachPlayerInRegion(plugin, { onlinePlayer ->
                if (onlinePlayer.uniqueId == player.uniqueId) {
                    return@forEachPlayerInRegion
                }

                if (!onlinePlayer.canVanishSee(player)) {
                    if (!onlinePlayer.canSee(player)) {
                        onlinePlayer.showPlayer(plugin, player)
                    }

                    spoofMessage?.let(onlinePlayer::sendMessage)
                } else {
                    onlinePlayer.sendMessage(visibleMessage)
                }
            }, true)
        }

        PlayerReappearEvent(player.uniqueId).call()
    }

    private fun spoofedConnectionMessage(player: Player, sign: String): Component? {
        if (!vanishConfig.spoofConnectionMessages) {
            return null
        }

        return miniMessage.deserialize(
            "<dark_gray>[$sign<dark_gray>] ${luckPermsPrefix(player.uniqueId)}${player.name}"
        )
    }

    fun isVanished(playerUuid: UUID) = vanishedPlayers.contains(playerUuid)

    fun all(): ObjectSet<UUID> = vanishedPlayers.snapshot().toObjectSet()

    fun allOnline(): ObjectSet<Player> =
        vanishedPlayers.snapshot().mapNotNull { Bukkit.getPlayer(it) }.toObjectSet()

    fun previous(player: Player): OfflinePlayer? {
        return playerQueues[player.uniqueId]?.back()?.let {
            Bukkit.getOfflinePlayer(it)
        }
    }

    fun next(player: Player): OfflinePlayer? {
        current(player)?.player?.let { currentPlayer ->
            SurfGlowingApi.removeGlowing(currentPlayer, player)
        }

        val next = playerQueues[player.uniqueId]?.next(
            Bukkit.getOnlinePlayers()
                .filter { it.uniqueId != player.uniqueId && player.canVanishSee(it) }
                .map { it.uniqueId }
                .toObjectList()
        )?.let {
            Bukkit.getOfflinePlayer(it)
        }

        next?.player?.let { nextPlayer ->
            SurfGlowingApi.makeGlowing(nextPlayer, player, VANISH_GLOW_COLOR)
        }

        return next
    }

    fun current(player: Player) =
        playerQueues[player.uniqueId]?.current?.let {
            Bukkit.getOfflinePlayer(it)
        }

    fun createAndShowScoreboard(player: Player) {
        scoreboards[player.uniqueId] = SurfScoreboardApi.createScoreboard(buildText {
            primary("    SpectateMode    ", TextDecoration.BOLD)
        })
            .addLine(buildText {
                info("Spieler")
            })
            .addUpdatableLine {
                buildText {
                    spacer(
                        player.currentTarget?.player?.name?.toSmallCaps()
                            ?: "Kein Spieler".toSmallCaps()
                    )
                }
            }
            .addEmptyLine()
            .addLine(buildText {
                info("Leben")
            })
            .addUpdatableLine {
                buildText {
                    spacer("${player.currentTarget?.player?.health?.toInt() ?: "Unbekannt"}".toSmallCaps() + "/" + "${player.currentTarget?.player?.healthScale?.toInt() ?: "Unbekannt"}".toSmallCaps())
                }
            }
            .addEmptyLine()
            .addLine(buildText {
                info("Entfernung")
            })
            .addUpdatableLine {
                buildText {
                    spacer(
                        "${
                            player.currentTarget?.player?.location?.distance(
                                player.location
                            )?.toInt() ?: "Unbekannt"
                        } Blöcke".toSmallCaps()
                    )
                }
            }
            .addEmptyLine()
            .addLine(buildText {
                info("Ping")
            })
            .addUpdatableLine {
                buildText {
                    spacer(
                        "${player.currentTarget?.player?.ping ?: "Unbekannt "}ms".toSmallCaps()
                    )
                }
            }
            .buildAutoUpdatable()

        scoreboards[player.uniqueId]?.enable()
        scoreboards[player.uniqueId]?.addViewer(player)
    }

    fun hideAndDeleteScoreboard(player: Player) {
        scoreboards[player.uniqueId]?.disable()
        scoreboards.remove(player.uniqueId)
    }

    fun setFlyState(uuid: UUID, canFly: Boolean) {
        playerFlyStates[uuid] = canFly
    }

    fun getFlyState(uuid: UUID): Boolean {
        return playerFlyStates[uuid] ?: false
    }

    fun isSpectating(playerUuid: UUID) = spectateModePlayers.contains(playerUuid)

    fun startSpectateMode(player: Player) {
        spectateModePlayers.add(player.uniqueId)

        createAndShowScoreboard(player)

        player.sendText {
            appendNewInfoPrefixedLine()
            darkSpacer("-".repeat(25))

            appendNewInfoPrefixedLine()
            spacer("Spectate-Mode Steuerung:".toSmallCaps())

            appendNewInfoPrefixedLine()
            appendNewInfoPrefixedLine()
            note("Zurück: ")
            displayKey("sneak")
            spacer(" + ")
            displayKey("swapOffhand")

            appendNewInfoPrefixedLine()
            note("Weiter: ")
            displayKey("swapOffhand")

            appendNewInfoPrefixedLine()
            note("Teleport: ")
            white("2x ")
            displayKey("sneak")

            appendNewInfoPrefixedLine()

            appendNewInfoPrefixedLine()
            darkSpacer("-".repeat(25))
        }
    }

    fun stopSpectateMode(player: Player) {
        spectateModePlayers.remove(player.uniqueId)

        hideAndDeleteScoreboard(player)
    }

    fun startActionbarTask() {
        if (actionbarTask?.isCancelled == false) {
            return
        }

        actionbarTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, {
            allOnline().forEach {
                it.sendActionBar(buildText {
                    note("Du bist für andere Spieler unsichtbar.")
                })
            }
        }, 0L, 1, TimeUnit.SECONDS)
    }

    fun stopActionbarTask() {
        actionbarTask?.cancel()
        actionbarTask = null
    }
}
