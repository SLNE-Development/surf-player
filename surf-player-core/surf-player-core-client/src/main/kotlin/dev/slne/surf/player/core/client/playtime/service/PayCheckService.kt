package dev.slne.surf.player.core.client.playtime.service

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.player.core.client.playtime.config.playtimeConfig
import dev.slne.surf.player.core.client.playtime.paycheck.PayCheckPayout
import dev.slne.surf.player.core.client.playtime.platform.PlaytimePlatform
import dev.slne.surf.player.core.client.playtime.rpc.playtimeRpcService
import dev.slne.surf.player.core.client.playtime.session.PlayerSessionEpochs
import net.kyori.adventure.audience.Audience
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

private val log = logger()

/**
 * Counts the playtime of the players on this server and pays out paychecks and event coins
 * whenever a configured interval is reached.
 *
 * Payouts need the optional surf-transaction plugin; [PayCheckPayout] is only touched when it is
 * available, so this class never loads the surf-transaction API classes otherwise.
 */
object PayCheckService {
    private val currentServerPlaytime = ConcurrentHashMap<UUID, AtomicLong>()

    suspend fun increasePlaytime(playerUuid: UUID, amount: Long) {
        val counter = currentServerPlaytime[playerUuid] ?: createCounter(playerUuid) ?: return

        handleUpdate(playerUuid, counter.addAndGet(amount))
    }

    /**
     * Loads the stored playtime of [playerUuid] on this server as the start of their counter.
     */
    suspend fun cachePlaytime(playerUuid: UUID) {
        val epoch = PlayerSessionEpochs.current(playerUuid)

        val seconds = playtimeRpcService.loadPlaytimeSecondsByServer(
            playerUuid,
            SurfServer.current().displayName
        )

        PlayerSessionEpochs.ifCurrent(playerUuid, epoch) {
            counterOf(playerUuid).set(seconds)
        }
    }

    fun invalidateCache(playerUuid: UUID) {
        currentServerPlaytime.remove(playerUuid)
    }

    private suspend fun handleUpdate(playerUuid: UUID, newPlaytime: Long) {
        val config = playtimeConfig
        val paycheck = config.paycheck
        val eventCoinPayout = config.eventCoinPayout

        if (paycheck.enabled && reachedInterval(newPlaytime, paycheck.intervalMinutes)) {
            payout(playerUuid, "paycheck") { PayCheckPayout.give(playerUuid, it) }
        }

        if (eventCoinPayout.enabled && reachedInterval(newPlaytime, eventCoinPayout.intervalMinutes)) {
            payout(playerUuid, "event coin payout") {
                PayCheckPayout.giveEventCoinPayout(playerUuid, it)
            }
        }
    }

    private fun reachedInterval(playtime: Long, intervalMinutes: Long) =
        playtime % (intervalMinutes.coerceAtLeast(1) * 60L) == 0L

    private suspend inline fun payout(
        playerUuid: UUID,
        kind: String,
        give: (Audience) -> Unit
    ) {
        if (!PlaytimePlatform.transactionAvailable) {
            log.atSevere()
                .log("Cannot give %s to %s: surf-transaction is not available", kind, playerUuid)
            return
        }

        give(PlaytimePlatform.onlinePlayer(playerUuid) ?: return)
    }

    private fun createCounter(playerUuid: UUID): AtomicLong? {
        var counter: AtomicLong? = null

        PlayerSessionEpochs.ifTracked(playerUuid) { counter = counterOf(playerUuid) }

        return counter
    }

    private fun counterOf(playerUuid: UUID) =
        currentServerPlaytime.computeIfAbsent(playerUuid) { AtomicLong() }
}
