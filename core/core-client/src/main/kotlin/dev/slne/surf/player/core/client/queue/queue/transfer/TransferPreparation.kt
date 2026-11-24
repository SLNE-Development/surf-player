package dev.slne.surf.player.core.client.queue.queue.transfer

import dev.slne.surf.core.api.common.player.SurfPlayer

/**
 * Whether a queued player can be sent to the queue's server right now.
 */
sealed interface TransferPreparation {
    class Ready(val player: SurfPlayer) : TransferPreparation
    class Rejected(val action: TransferAction) : TransferPreparation
}
