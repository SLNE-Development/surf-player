package dev.slne.surf.social.minestom.profile.menu

import dev.slne.surf.api.core.messages.adventure.playSound
import me.devnatan.inventoryframework.context.SlotClickContext
import net.minestom.server.sound.SoundEvent

fun SlotClickContext.playClickSound() {
    this.player.playSound(true) {
        type(SoundEvent.UI_BUTTON_CLICK)
    }
}
