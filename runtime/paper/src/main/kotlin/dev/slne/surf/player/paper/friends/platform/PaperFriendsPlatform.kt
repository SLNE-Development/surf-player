package dev.slne.surf.player.paper.friends.platform

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.auto.service.AutoService
import dev.slne.surf.player.core.client.friends.platform.FriendsPlatform
import dev.slne.surf.player.core.client.vanish.CommandViewer
import dev.slne.surf.player.paper.command.asViewer
import dev.slne.surf.player.paper.plugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import java.util.*

@AutoService(FriendsPlatform::class)
class PaperFriendsPlatform : FriendsPlatform {
    override fun isOnline(uuid: UUID) = Bukkit.getPlayer(uuid) != null

    override fun viewer(uuid: UUID): CommandViewer? = Bukkit.getPlayer(uuid)?.asViewer()

    override fun sendMessage(uuid: UUID, message: Component) {
        Bukkit.getPlayer(uuid)?.sendMessage(message)
    }

    override fun playSound(uuid: UUID, sound: Sound) {
        Bukkit.getPlayer(uuid)?.playSound(sound)
    }

    override fun launch(block: suspend CoroutineScope.() -> Unit): Job =
        plugin.launch(context = plugin.globalRegionDispatcher, block = block)
}
