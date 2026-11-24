package dev.slne.surf.player.core.common.playtime.bridge

import dev.slne.surf.api.core.util.requiredService
import java.util.*

val transactionBridge = requiredService<TransactionBridge>()

interface TransactionBridge {
    suspend fun givePaycheck(playerUuid: UUID)
    suspend fun giveEventCoinPayout(playerUuid: UUID)
}