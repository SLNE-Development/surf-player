package dev.slne.surf.player.paper.vanish.service

import com.destroystokyo.paper.profile.ProfileProperty
import dev.slne.surf.api.core.luckperms.LuckPermsAccess
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.api.paper.util.getPrefixedName
import dev.slne.surf.player.api.vanish.event.PlayerNickEvent
import dev.slne.surf.player.api.vanish.event.PlayerUnNickEvent
import dev.slne.surf.player.paper.vanish.util.retrieveSkin
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object NickService {
    private val nickedPlayers = ConcurrentHashMap<UUID, String>()
    private val oldTextures = ConcurrentHashMap<UUID, ProfileProperty>()

    fun isNicked(player: Player) = nickedPlayers.containsKey(player.uniqueId)

    suspend fun nick(player: Player, nickname: String) {
        val textures = retrieveSkin(nickname)

        oldTextures[player.uniqueId] =
            player.playerProfile.properties.find { it.name == "textures" } ?: return
        nickedPlayers[player.uniqueId] = nickname

        player.playerProfile = player.playerProfile.apply {
            setProperty(
                ProfileProperty(
                    "textures",
                    textures?.value ?: "",
                    textures?.signature ?: ""
                )
            )
        }

        val defaultPrefix =
            LuckPermsAccess.luckperms.groupManager.getGroup("default")?.cachedData?.metaData?.prefix ?: ""
        player.displayName(miniMessage.deserialize(defaultPrefix + nickname))

        PlayerNickEvent(player.uniqueId, nickname).call()
    }

    fun unnick(player: Player) {
        player.playerProfile = player.playerProfile.apply {
            oldTextures[player.uniqueId]?.let { setProperty(it) }
        }

        nickedPlayers.remove(player.uniqueId)
        oldTextures.remove(player.uniqueId)

        player.displayName(player.getPrefixedName())
        PlayerUnNickEvent(player.uniqueId).call()
    }
}
