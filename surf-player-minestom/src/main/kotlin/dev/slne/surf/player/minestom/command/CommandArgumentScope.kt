package dev.slne.surf.player.minestom.command

import dev.slne.surf.api.core.util.logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val log = logger()

/**
 * The scope command arguments resolve their values on, so the lookup can run while the command is
 * being parsed.
 */
internal val commandArgumentScope = CoroutineScope(
    SupervisorJob() +
            Dispatchers.Default +
            CoroutineName("CommandArguments") +
            CoroutineExceptionHandler { _, throwable ->
                log.atWarning()
                    .withCause(throwable)
                    .log("An error occurred while resolving a command argument")
            }
)
