package dev.slne.surf.player.core.client.contentcreator.command

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.player.api.contentcreator.platform.PlatformState
import dev.slne.surf.player.api.contentcreator.platform.PlatformType
import dev.slne.surf.player.core.client.contentcreator.service.ContentCreatorService
import net.kyori.adventure.audience.Audience
import java.util.*

/**
 * Backs the debug command that forces the live state of a content creator.
 */
object LiveStateDebugCommand {

    internal const val NOT_CONNECTED_MESSAGE = "Du bist nicht mit Twitch verbunden."

    /**
     * Forces the twitch state of the content creator behind [playerUuid] to [newState] and reports
     * the outcome to [audience].
     *
     * A player who is no cached creator yet has their Twitch link read again first ([loadCreator]),
     * since links are otherwise only read when joining: a link made while online works right away.
     */
    suspend fun setTwitchState(
        audience: Audience,
        playerUuid: UUID,
        newState: PlatformState,
        loadCreator: suspend (UUID) -> Unit = ContentCreatorService::cacheCreator,
    ) {
        if (ContentCreatorService.getContentCreator(playerUuid) == null) {
            loadCreator(playerUuid)
        }

        val updated = ContentCreatorService.updatePlatformState(
            playerUuid,
            PlatformType.TWITCH,
            newState
        )

        if (!updated) {
            audience.sendText {
                appendErrorPrefix()
                error(NOT_CONNECTED_MESSAGE)
            }
            return
        }

        audience.sendText {
            appendSuccessPrefix()
            success(stateChangedMessage(newState))
        }
    }

    internal fun stateChangedMessage(newState: PlatformState) =
        "Der Live-Status wurde erfolgreich auf ${newState.name} gesetzt."
}
