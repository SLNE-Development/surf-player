package dev.slne.surf.player.minestom.playtime.platform

import com.google.auto.service.AutoService
import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.player.core.client.playtime.platform.PlaytimePlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@AutoService(PlaytimePlatform::class)
class MinestomPlaytimePlatform : PlaytimePlatform {
    /** Whether the surf-transaction API is on the classpath of this server. */
    override val transactionAvailable by lazy {
        runCatching { Class.forName("dev.slne.surf.transaction.api.user.TransactionUser") }.isSuccess
    }

    override fun onlinePlayer(playerUuid: UUID) = ConnectionManager.getOnlinePlayerByUuid(playerUuid)

    override fun launchAsync(block: suspend CoroutineScope.() -> Unit) =
        minestomAsyncScope.launch(block = block)
}
