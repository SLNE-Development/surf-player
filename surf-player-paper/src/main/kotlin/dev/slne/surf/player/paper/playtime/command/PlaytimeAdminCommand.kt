package dev.slne.surf.player.paper.playtime.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.player.core.client.playtime.command.*
import dev.slne.surf.player.core.client.playtime.config.PlaytimeConfigManager
import dev.slne.surf.player.core.client.playtime.permission.PlaytimePermissions
import dev.slne.surf.player.core.client.playtime.service.PlaytimeStreakService
import dev.slne.surf.player.paper.plugin
import kotlinx.coroutines.Deferred

fun playtimeAdminCommand() = commandTree("playtimeadmin") {
    withPermission(PlaytimePermissions.COMMAND_ADMIN)
    literalArgument("reload") {
        anyExecutor { sender, _ ->
            PlaytimeConfigManager.reload()

            sender.sendConfigurationReloaded()
        }
    }

    literalArgument("streak") {
        literalArgument("recalculateall") {
            anyExecutor { sender, _ ->
                sender.sendRecalculateAllStarted()

                plugin.launch {
                    val count = PlaytimeStreakService.recalculateAllPlaytimeStreaks()

                    sender.sendRecalculateAllFinished(count)
                }
            }
        }

        literalArgument("recalculate") {
            surfOfflinePlayerArgument("player") {
                anyExecutor { sender, args ->
                    val player: Deferred<SurfPlayer?> by args

                    plugin.launch {
                        val targetPlayer = player.await()
                        if (targetPlayer == null) {
                            sender.sendPlayerNotFound()
                            return@launch
                        }

                        val streak =
                            PlaytimeStreakService.recalculatePlaytimeStreak(targetPlayer.uuid)

                        if (streak == null) {
                            sender.sendNoSessionsFound(targetPlayer.lastKnownName ?: "Unbekannt")
                            return@launch
                        }

                        PlaytimeStreakService.invalidateCache(targetPlayer.uuid)

                        sender.sendStreakRecalculated(
                            playerName = targetPlayer.lastKnownName ?: "Unbekannt",
                            currentLoginStreak = streak.currentLoginStreak,
                            longestLoginStreak = streak.longestLoginStreak
                        )
                    }
                }
            }
        }
    }

    literalArgument("streakpause") {
        literalArgument("create") {
            stringArgument("von") {
                stringArgument("bis") {
                    anyExecutor { sender, args ->
                        val von: String by args
                        val bis: String by args

                        val startDate = parseDate(von)
                        val endDate = parseDate(bis)

                        if (startDate == null || endDate == null) {
                            sender.sendInvalidDate()
                            return@anyExecutor
                        }

                        if (endDate.isBefore(startDate)) {
                            sender.sendEndDateBeforeStartDate()
                            return@anyExecutor
                        }

                        plugin.launch {
                            val pause =
                                PlaytimeStreakService.createStreakPause(startDate, endDate)

                            sender.sendStreakPauseCreated(pause)
                        }
                    }
                }
            }
        }

        literalArgument("delete") {
            longArgument("id") {
                anyExecutor { sender, args ->
                    val id: Long by args

                    plugin.launch {
                        val deleted = PlaytimeStreakService.deleteStreakPause(id)

                        if (deleted) {
                            sender.sendStreakPauseDeleted(id)
                        } else {
                            sender.sendStreakPauseNotFound(id)
                        }
                    }
                }
            }
        }

        literalArgument("list") {
            anyExecutor { sender, _ ->
                plugin.launch {
                    val pauses = PlaytimeStreakService.loadStreakPauses()

                    if (pauses.isEmpty()) {
                        sender.sendNoStreakPauses()
                        return@launch
                    }

                    sender.sendStreakPauseList(pauses)
                }
            }
        }
    }
}
