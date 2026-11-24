package dev.slne.surf.social.velocity.clan.listener

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.social.api.clan.invite.ClanInvite
import dev.slne.surf.social.core.client.clan.components.ClanComponents
import dev.slne.surf.social.core.common.clan.clan.ClanImpl
import dev.slne.surf.social.velocity.plugin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Duration.Companion.seconds

private const val MAX_CONCURRENT_CLAN_LOOKUPS = 64

/**
 * Lists the pending clan invitations of a player once they have joined the network.
 */
object JoinInviteListener {
    @Subscribe
    fun onServerConnected(event: ServerConnectedEvent) {
        if (event.previousServer.isPresent) return

        plugin.container.launch {
            delay(1.seconds)

            val player = event.player
            if (player.currentServer.isEmpty) {
                return@launch
            }

            val invites = ClanInvite.pendingInvitesByPlayer(player.uniqueId)

            if (invites.isEmpty()) {
                return@launch
            }

            val renderedInvites = renderInvites(invites)

            player.sendText {
                appendInfoPrefix()
                info("Du hast noch ".toSmallCaps())
                variableValue(invites.size)
                info(" offene Clan-Einladung${if (invites.size == 1) "" else "en"}.".toSmallCaps())
                appendCollectionNewLine(renderedInvites) { it.asComponent() }
            }
        }
    }

    /**
     * Renders [invites] in their order, leaving out invitations whose clan no longer exists.
     */
    private suspend fun renderInvites(invites: List<ClanInvite>): List<ClanInviteRenderData> {
        val rendered = arrayOfNulls<ClanInviteRenderData>(invites.size)
        val semaphore = Semaphore(MAX_CONCURRENT_CLAN_LOOKUPS)

        supervisorScope {
            invites.forEachIndexed { slot, invite ->
                launch {
                    semaphore.withPermit {
                        val clan = invite.getClan() ?: return@launch
                        rendered[slot] = ClanInviteRenderData(
                            clanInformationHover = ClanComponents.renderClanInformationHover(
                                clan as ClanImpl
                            ),
                            clanName = clan.name
                        )
                    }
                }
            }
        }

        return rendered.filterNotNull()
    }
}
