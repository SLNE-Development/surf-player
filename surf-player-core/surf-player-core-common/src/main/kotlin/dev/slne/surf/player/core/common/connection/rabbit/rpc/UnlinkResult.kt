package dev.slne.surf.social.core.common.connection.rabbit.rpc

import kotlinx.serialization.Serializable

/**
 * The outcome of [SocialConnectionRpc.unlinkMinecraftAccount].
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
