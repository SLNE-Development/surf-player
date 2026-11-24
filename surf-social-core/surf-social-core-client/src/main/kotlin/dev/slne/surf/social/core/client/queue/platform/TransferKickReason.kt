package dev.slne.surf.social.core.client.queue.platform

/**
 * Reason a server refused a player's connection attempt.
 */
enum class TransferKickReason {
    FULL_SERVER,
    NOT_WHITELISTED,
    OTHER
}
