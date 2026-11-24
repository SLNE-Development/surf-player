package dev.slne.surf.social.minestom.playtime.hook

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.client.playtime.paycheck.PayCheckPayout
import dev.slne.surf.social.core.client.playtime.platform.PlaytimePlatform
import dev.slne.surf.social.core.common.playtime.bridge.TransactionBridge
import java.util.*

@AutoService(TransactionBridge::class)
class TransactionBridgeImpl : TransactionBridge {
    override suspend fun givePaycheck(playerUuid: UUID) {
        val player = PlaytimePlatform.onlinePlayer(playerUuid) ?: return
        PayCheckPayout.give(playerUuid, player)
    }

    override suspend fun giveEventCoinPayout(playerUuid: UUID) {
        val player = PlaytimePlatform.onlinePlayer(playerUuid) ?: return
        PayCheckPayout.giveEventCoinPayout(playerUuid, player)
    }
}
