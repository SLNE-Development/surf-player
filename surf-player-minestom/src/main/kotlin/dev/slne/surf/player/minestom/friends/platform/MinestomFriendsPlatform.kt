package dev.slne.surf.social.minestom.friends.platform

import com.google.auto.service.AutoService
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.social.core.client.friends.platform.FriendsPlatform
import net.kyori.adventure.text.Component
import java.util.*

@AutoService(FriendsPlatform::class)
class MinestomFriendsPlatform : FriendsPlatform {
    override fun isOnline(uuid: UUID) = ConnectionManager.getOnlinePlayerByUuid(uuid) != null

    override fun sendMessage(uuid: UUID, message: Component) {
        ConnectionManager.getOnlinePlayerByUuid(uuid)?.sendMessage(message)
    }
}
