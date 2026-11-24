package dev.slne.surf.player.core.client.queue.permission

/**
 * Every permission node of the queue feature, the same on every platform (Paper, Minestom and
 * Velocity).
 *
 * See docs/command-conventions.md for the scheme.
 */
object QueuePermissions {
    private const val ADMIN = "surf.player.admin.queue"

    // Admin actions (/surfplayer queue ..., /surfplayervelocity queue ...)
    const val ADMIN_RELOAD = "$ADMIN.reload"
    const val ADMIN_LIST = "$ADMIN.list"
    const val ADMIN_INFO = "$ADMIN.info"
    const val ADMIN_ENQUEUE = "$ADMIN.enqueue"
    const val ADMIN_DEQUEUE = "$ADMIN.dequeue"
    const val ADMIN_CLEAR = "$ADMIN.clear"
    const val ADMIN_CLEANUP = "$ADMIN.cleanup"
    const val ADMIN_FIX = "$ADMIN.fix"
    const val ADMIN_PAUSE = "$ADMIN.pause"
    const val ADMIN_RESUME = "$ADMIN.resume"
    const val ADMIN_STATUS = "$ADMIN.status"
    const val ADMIN_METRICS = "$ADMIN.metrics"
}
