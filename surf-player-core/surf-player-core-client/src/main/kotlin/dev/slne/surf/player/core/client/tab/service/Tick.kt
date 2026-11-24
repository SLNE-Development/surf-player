package dev.slne.surf.social.core.client.tab.service

import kotlin.time.Duration

/**
 * Describes the next wake-up of the [TablistScheduler].
 *
 * @property delay the duration to wait before the scheduler wakes up
 * @property reason the reason to invalidate the tab list after waking, or `null` when the
 * scheduler should only recalculate its next tick
 */
class Tick(val delay: Duration, val reason: TablistUpdateReason?)
