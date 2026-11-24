package dev.slne.surf.social.paper.friends.platform

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.client.friends.platform.FriendsPlatform
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import java.util.*

@AutoService(FriendsPlatform::class)
class PaperFriendsPlatform : FriendsPlatform {
    override fun isOnline(uuid: UUID) = Bukkit.getPlayer(uuid) != null

    override fun sendMessage(uuid: UUID, message: Component) {
        Bukkit.getPlayer(uuid)?.sendMessage(message)
    }
}
