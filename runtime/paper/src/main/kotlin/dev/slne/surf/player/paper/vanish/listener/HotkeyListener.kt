package dev.slne.surf.player.paper.vanish.listener

import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.DiggingAction
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerInput
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.player.paper.vanish.service.VanishService
import dev.slne.surf.player.paper.vanish.util.SpectateHotkeys
import dev.slne.surf.player.paper.vanish.util.isVanished
import org.bukkit.entity.Player
import java.util.*

object HotkeyListener : PacketListenerAbstract() {
    private val hotkeys = SpectateHotkeys()

    /** Forgets the sneak state of [player], who stopped spectating or left. */
    fun forget(player: UUID) = hotkeys.forget(player)

    override fun onPacketReceive(event: PacketReceiveEvent) {
        val player = event.getPlayer<Player>() ?: return

        if (!player.isVanished) {
            return
        }

        if (!VanishService.isSpectating(player.uniqueId)) {
            return
        }

        when (event.packetType) {
            PacketType.Play.Client.PLAYER_INPUT -> {
                val packet = WrapperPlayClientPlayerInput(event)

                if (hotkeys.onInput(player.uniqueId, packet.isShift, System.currentTimeMillis())) {
                    val current = VanishService.current(player) ?: run {
                        player.sendText {
                            appendErrorPrefix()
                            error("Du schaust gerade niemandem zu.")
                        }
                        return
                    }

                    val vanishTarget = current.player ?: run {
                        player.sendText {
                            appendErrorPrefix()
                            error("Der Spieler, dem du zuschaust, ist nicht mehr online.")
                        }
                        return
                    }

                    player.teleportAsync(vanishTarget.location)
                    player.sendText {
                        appendSuccessPrefix()
                        success("Du bist nun wieder bei")
                        appendSpace()
                        variableValue(vanishTarget.name)
                        success(".")
                    }
                }
            }

            PacketType.Play.Client.PLAYER_DIGGING -> {
                val packet = WrapperPlayClientPlayerDigging(event)

                if (packet.action == DiggingAction.SWAP_ITEM_WITH_OFFHAND) {
                    if (hotkeys.isSneaking(player.uniqueId)) {
                        val previous = VanishService.previous(player) ?: run {
                            player.sendText {
                                appendErrorPrefix()
                                error("Es wurde kein Spieler gefunden, den du zuvor angeschaut hast.")
                            }
                            return
                        }

                        val vanishTarget = previous.player ?: run {
                            player.sendText {
                                appendErrorPrefix()
                                error("Der Spieler, dem du zuschauen möchtest, ist nicht mehr online.")
                            }
                            return
                        }

                        player.teleportAsync(vanishTarget.location)
                        player.sendText {
                            appendSuccessPrefix()
                            success("Du schaust nun wieder")
                            appendSpace()
                            variableValue(vanishTarget.name)
                            appendSpace()
                            success("zu.")
                        }
                        return
                    }

                    val next = VanishService.next(player) ?: run {
                        player.sendText {
                            appendErrorPrefix()
                            error("Es wurde kein weiterer Spieler gefunden, dem du zuschauen könntest.")
                        }
                        return
                    }

                    val vanishTarget = next.player ?: run {
                        player.sendText {
                            appendErrorPrefix()
                            error("Der Spieler, dem du zuschauen möchtest, ist nicht mehr online.")
                        }
                        return
                    }

                    player.teleportAsync(vanishTarget.location)
                    player.sendText {
                        appendSuccessPrefix()
                        success("Du schaust nun")
                        appendSpace()
                        variableValue(vanishTarget.name)
                        appendSpace()
                        success("zu.")
                    }
                }
            }
        }
    }
}