package dev.slne.surf.player.minestom.hook

import dev.slne.surf.api.core.luckperms.LuckPermsAccess
import net.luckperms.api.event.node.NodeAddEvent
import net.luckperms.api.event.node.NodeRemoveEvent
import net.luckperms.api.event.user.UserDataRecalculateEvent
import net.luckperms.api.model.user.User

/**
 * Calls [onChange] whenever the LuckPerms data (nodes, prefix, groups, ...) of a user changes.
 */
fun subscribeToLuckPermsUserChanges(onChange: (User) -> Unit) {
    val eventBus = LuckPermsAccess.luckperms.eventBus

    eventBus.subscribe(NodeAddEvent::class.java) { event ->
        (event.target as? User)?.let(onChange)
    }

    eventBus.subscribe(NodeRemoveEvent::class.java) { event ->
        (event.target as? User)?.let(onChange)
    }

    eventBus.subscribe(UserDataRecalculateEvent::class.java) { event ->
        onChange(event.user)
    }
}
