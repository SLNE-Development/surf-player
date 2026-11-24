package dev.slne.surf.social.minestom.nametag.platform

import com.google.auto.service.AutoService
import dev.slne.surf.api.minestom.coroutine.minestomScope
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.api.minestom.inventory.framework.view.util.shift as glyphShift
import dev.slne.surf.social.core.client.nametag.platform.NametagPlatform
import dev.slne.surf.social.minestom.nametag.NametagMinestomFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@AutoService(NametagPlatform::class)
class MinestomNametagPlatform : NametagPlatform {
    override val dataPath get() = NametagMinestomFeature.dataDirectory

    override val clanAvailable = true
    override val contentCreatorAvailable = true

    override fun onlinePlayers() =
        ConnectionManager.onlinePlayers.map { MinestomNametagPlayer(it) }

    override fun player(playerUuid: UUID) =
        ConnectionManager.getOnlinePlayerByUuid(playerUuid)?.let { MinestomNametagPlayer(it) }

    override fun shift(amount: Int) = glyphShift(amount)

    override fun launch(block: suspend CoroutineScope.() -> Unit) =
        minestomScope.launch(block = block)
}
