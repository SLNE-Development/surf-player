package dev.slne.surf.player.core.client.chat.service

import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import java.util.*

/**
 * What an ignore entry does, with team members ([ChatPermissions.IGNORE_EXEMPT]) exempt from it.
 */
object IgnoreRules {
    /**
     * Whether a message (chat, private message) from a sender is kept from a receiver: only if the
     * receiver [ignoresSender] and the sender is not [senderExempt] (asked only then). An ignore
     * entry that exists from before the sender became a team member has no effect.
     */
    inline fun blocksMessage(ignoresSender: Boolean, senderExempt: () -> Boolean): Boolean =
        ignoresSender && !senderExempt()

    /**
     * Whether a direct message is kept from its receiver because the receiver ignores the sender,
     * wherever both are: the ignore list of a receiver on this server is cached here
     * ([cachedIgnore]); one on another server is asked from the microservice ([storedIgnore]). A
     * team member's ([senderExempt]) message is never blocked, and then nothing is looked up.
     */
    suspend fun blocksDirectMessage(
        receiverOnThisServer: Boolean,
        senderExempt: () -> Boolean,
        cachedIgnore: () -> Boolean,
        storedIgnore: suspend () -> Boolean,
    ): Boolean {
        if (senderExempt()) return false

        return if (receiverOnThisServer) cachedIgnore() else storedIgnore()
    }

    /**
     * Why [player] cannot ignore [target] (called [targetName]), or `null` if they can.
     *
     * @param alreadyIgnored whether [player] already ignores [target]
     * @param targetExempt whether [target] is a team member, asked only when every other check
     * passed
     */
    inline fun ignoreRefusal(
        player: UUID,
        target: UUID,
        targetName: String,
        alreadyIgnored: Boolean,
        targetExempt: () -> Boolean,
    ): String? = when {
        player == target -> "Du kannst dich nicht selbst ignorieren."
        alreadyIgnored -> "Du ignorierst $targetName bereits."
        targetExempt() -> "Du kannst $targetName nicht ignorieren."
        else -> null
    }
}
