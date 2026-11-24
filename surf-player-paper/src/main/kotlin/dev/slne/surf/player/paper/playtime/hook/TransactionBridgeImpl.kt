package dev.slne.surf.player.paper.playtime.hook

import com.google.auto.service.AutoService
import dev.slne.surf.player.core.client.playtime.paycheck.PayCheckPayout
import dev.slne.surf.player.core.common.playtime.bridge.TransactionBridge
import dev.slne.surf.player.paper.playtime.hasTransactionHook
import dev.slne.surf.player.paper.playtime.plugin
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import java.util.*

@AutoService(TransactionBridge::class)
class TransactionBridgeImpl : TransactionBridge, Services.Fallback {
    override suspend fun givePaycheck(playerUuid: UUID) {
        if (hasTransactionHook) {
            val player = Bukkit.getPlayer(playerUuid) ?: return
            PayCheckPayout.give(playerUuid, player)
        } else {
            plugin.logger.severe("Attempted to give paycheck to player $playerUuid, but no transaction hook is available.")
        }
    }

    override suspend fun giveEventCoinPayout(playerUuid: UUID) {
        if (hasTransactionHook) {
            val player = Bukkit.getPlayer(playerUuid) ?: return
            PayCheckPayout.giveEventCoinPayout(playerUuid, player)
        } else {
            plugin.logger.severe("Attempted to give event coin payout to player $playerUuid, but no transaction hook is available.")
        }
    }
}
