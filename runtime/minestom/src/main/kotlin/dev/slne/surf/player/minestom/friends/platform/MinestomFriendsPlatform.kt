package dev.slne.surf.player.minestom.friends.platform

import com.google.auto.service.AutoService
import dev.slne.surf.api.minestom.coroutine.MinestomDispatchers
import dev.slne.surf.api.minestom.coroutine.minestomScope
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.player.core.client.friends.platform.FriendsPlatform
import dev.slne.surf.player.core.client.vanish.CommandViewer
import dev.slne.surf.player.minestom.command.asViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import java.util.*

@AutoService(FriendsPlatform::class)
class MinestomFriendsPlatform : FriendsPlatform {
    override fun isOnline(uuid: UUID) = ConnectionManager.getOnlinePlayerByUuid(uuid) != null

    override fun viewer(uuid: UUID): CommandViewer? = ConnectionManager.getOnlinePlayerByUuid(uuid)?.asViewer()

    override fun sendMessage(uuid: UUID, message: Component) {
        ConnectionManager.getOnlinePlayerByUuid(uuid)?.sendMessage(message)
    }

    override fun playSound(uuid: UUID, sound: Sound) {
        ConnectionManager.getOnlinePlayerByUuid(uuid)?.playSound(sound)
    }

    override fun launch(block: suspend CoroutineScope.() -> Unit): Job =
        minestomScope.launch(context = MinestomDispatchers.Main, block = block)
}
