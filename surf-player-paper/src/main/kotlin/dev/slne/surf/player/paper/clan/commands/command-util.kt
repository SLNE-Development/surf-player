package dev.slne.surf.player.paper.clan.commands

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPIPaper
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.player.paper.plugin
import kotlinx.coroutines.CoroutineScope
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player
import java.time.Duration

fun failCommand(message: String): Nothing =
    throw CommandAPIPaper.failWithAdventureComponent(buildText {
        appendErrorPrefix()
        error(message)
    })

/**
 * A click event that launches [onClick] when a player clicks it within [lifetime].
 */
fun playerClickEvent(
    lifetime: Duration = ClickCallback.DEFAULT_LIFETIME,
    onClick: suspend CoroutineScope.(Player) -> Unit
): ClickEvent<*> = ClickEvent.callback(
    ClickCallback.widen({ clicked ->
        plugin.launch { onClick(clicked) }
    }, Player::class.java)
) { it.lifetime(lifetime) }
