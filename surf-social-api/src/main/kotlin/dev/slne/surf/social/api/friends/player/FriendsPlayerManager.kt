package dev.slne.surf.social.api.friends.player

import dev.slne.surf.api.core.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.jetbrains.annotations.UnmodifiableView
import java.util.*

private val instance = requiredService<FriendsPlayerManager>()

interface FriendsPlayerManager {
    val players: @UnmodifiableView ObjectSet<FriendsPlayer>

    fun findPlayer(uuid: UUID): FriendsPlayer
    fun invalidatePlayer(uuid: UUID)

    companion object : FriendsPlayerManager by instance {
        val INSTANCE get() = instance
    }
}
