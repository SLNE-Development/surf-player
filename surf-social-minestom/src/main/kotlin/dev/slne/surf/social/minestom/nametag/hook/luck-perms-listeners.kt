package dev.slne.surf.social.minestom.nametag.hook

import dev.slne.surf.social.core.client.nametag.hook.LuckPermsHook
import net.luckperms.api.event.node.NodeAddEvent
import net.luckperms.api.event.node.NodeRemoveEvent
import net.luckperms.api.event.user.UserDataRecalculateEvent
import net.luckperms.api.model.user.User

fun registerLuckPermsListeners() {
    val eventBus = LuckPermsHook.luckPerms.eventBus

    eventBus.subscribe(NodeAddEvent::class.java) { event ->
        val user = event.target as? User ?: return@subscribe
        LuckPermsHook.updatePlayer(user)
    }

    eventBus.subscribe(NodeRemoveEvent::class.java) { event ->
        val user = event.target as? User ?: return@subscribe
        LuckPermsHook.updatePlayer(user)
    }

    eventBus.subscribe(UserDataRecalculateEvent::class.java) { event ->
        LuckPermsHook.updatePlayer(event.user)
    }
}
