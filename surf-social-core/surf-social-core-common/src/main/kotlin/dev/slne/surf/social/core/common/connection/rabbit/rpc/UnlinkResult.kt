package dev.slne.surf.social.core.common.connection.rabbit.rpc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The outcome of [SocialConnectionRpc.unlinkMinecraftAccount].
 *
 * The serial names are the ones this type had while it was nested in [SocialConnectionRpc], so the
 * wire format is unchanged.
 */
@Serializable
@SerialName("dev.slne.surf.social.core.common.connection.rabbit.rpc.SocialConnectionRpc.UnlinkResult")
sealed interface UnlinkResult {
    @Serializable
    @SerialName("dev.slne.surf.social.core.common.connection.rabbit.rpc.SocialConnectionRpc.UnlinkResult.NotLinked")
    data object NotLinked : UnlinkResult

    @Serializable
    @SerialName("dev.slne.surf.social.core.common.connection.rabbit.rpc.SocialConnectionRpc.UnlinkResult.Unlinked")
    data class Unlinked(val discordId: Long?) : UnlinkResult
}
