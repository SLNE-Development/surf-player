package dev.slne.surf.player.core.common.connection.rpc

import kotlinx.serialization.Serializable

/**
 * The outcome of [ConnectionRpcService.deleteMinecraftLink].
 */
@Serializable
sealed interface UnlinkResult {
    /** No Minecraft account was linked. */
    @Serializable
    data object NotLinked : UnlinkResult

    /** The link was removed; [discordId] is the Discord account of the same user, if any. */
    @Serializable
    data class Unlinked(val discordId: Long?) : UnlinkResult
}
