package dev.slne.surf.player.core.client.friends.command

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.player.core.client.command.CommandOutcome
import dev.slne.surf.player.core.client.command.failure
import dev.slne.surf.player.core.client.command.reply
import dev.slne.surf.player.core.client.friends.rpc.friendsRpcService
import java.util.*

/**
 * The admin actions of `/surfplayer friends` every platform runs.
 */
object FriendAdminCommands {
    /** The description of `/surfplayer friends cooldown clear <spieler> [ziel]`. */
    const val COOLDOWN_CLEAR_DESCRIPTION =
        "Löscht die Freundschaftsanfrage-Cooldowns eines Spielers (nur gegenüber ziel, falls angegeben)"
    const val COOLDOWN_CLEAR_ARGUMENTS = "clear <spieler> [ziel]"

    /**
     * Deletes the friend request cooldowns of [player] (named [playerName]), only the one towards
     * [target] (named [targetName]) if given.
     */
    suspend fun clearCooldowns(player: UUID, playerName: String, target: UUID?, targetName: String?): CommandOutcome {
        if (player == target) return failure("Ein Spieler hat keinen Cooldown gegenüber sich selbst.")

        val deleted = friendsRpcService.deleteFriendRequestCooldowns(player, target)

        return cooldownsCleared(deleted, playerName, targetName)
    }

    /** The answer to deleting [deleted] cooldowns of [playerName] (towards [targetName]). */
    fun cooldownsCleared(deleted: Int, playerName: String, targetName: String?): CommandOutcome {
        val towards = targetName?.let { " gegenüber $it" } ?: ""

        if (deleted == 0) return failure("$playerName hat keine Freundschaftsanfrage-Cooldowns$towards.")

        return reply(buildText {
            appendSuccessPrefix()
            success(if (deleted == 1) "Ein Freundschaftsanfrage-Cooldown" else "$deleted Freundschaftsanfrage-Cooldowns")
            success(" von ")
            variableValue(playerName)
            if (targetName != null) {
                success(" gegenüber ")
                variableValue(targetName)
            }
            success(if (deleted == 1) " wurde gelöscht." else " wurden gelöscht.")
        })
    }
}
