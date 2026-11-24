package dev.slne.surf.player.paper.hook

import dev.slne.surf.api.core.luckperms.LuckPermsAccess
import dev.slne.surf.player.paper.plugin
import net.luckperms.api.event.node.NodeAddEvent
import net.luckperms.api.event.node.NodeRemoveEvent
import net.luckperms.api.event.user.UserDataRecalculateEvent
import net.luckperms.api.model.user.User

/**
 * Calls [onChange] whenever a LuckPerms user's nodes or cached data change.
 */
fun subscribeToLuckPermsUserChanges(onChange: (User) -> Unit) {
    val eventBus = LuckPermsAccess.luckperms.eventBus

    eventBus.subscribe(plugin, NodeAddEvent::class.java) { event ->
        (event.target as? User)?.let(onChange)
    }

    eventBus.subscribe(plugin, NodeRemoveEvent::class.java) { event ->
        (event.target as? User)?.let(onChange)
    }

    eventBus.subscribe(plugin, UserDataRecalculateEvent::class.java) { event ->
        onChange(event.user)
    }
}
