package dev.slne.surf.player.paper.chat.util

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.player.paper.plugin
import org.bukkit.Sound
import org.bukkit.entity.Player

/** Plays the chat ping sound (mentions, direct messages) for this player. */
fun Player.playPingSound() {
    playSound(true) {
        type(Sound.ENTITY_CHICKEN_EGG)
    }
}

/** Plays the chat ping sound on this player's entity thread. */
fun Player.schedulePingSound() {
    plugin.launch(plugin.entityDispatcher(this)) {
        playPingSound()
    }
}
