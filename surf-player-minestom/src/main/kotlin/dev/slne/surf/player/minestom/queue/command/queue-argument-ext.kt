package dev.slne.surf.player.minestom.queue.command

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.minestom.command.failCommand
import kotlinx.coroutines.Deferred

/**
 * Awaits the player resolved by a `surfOfflinePlayerArgument`, failing the command with a
 * user-facing message when no player matches the given input.
 */
suspend fun Deferred<SurfPlayer?>.awaitOrFail(): SurfPlayer =
    await() ?: failCommand("Der Spieler wurde nicht gefunden.")
