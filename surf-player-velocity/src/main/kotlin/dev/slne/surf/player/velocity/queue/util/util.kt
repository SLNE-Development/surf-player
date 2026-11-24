package dev.slne.surf.player.velocity.queue.util

import dev.slne.surf.player.velocity.queue.plugin
import java.util.*
import kotlin.jvm.optionals.getOrNull

fun UUID.toVelocityPlayer() = plugin.proxy.getPlayer(this).getOrNull()