package dev.slne.surf.player.paper.nametag.platform

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams
import dev.slne.surf.player.api.nametag.nametag.Nametag
import dev.slne.surf.player.core.client.nametag.config.NametagConfig
import dev.slne.surf.player.core.client.nametag.platform.NametagPlayer
import dev.slne.surf.player.core.client.nametag.service.teamDisplayName
import dev.slne.surf.player.paper.nametag.util.sendPacket
import org.bukkit.entity.Player
import java.util.*

private val nullScoreboardInfo: WrapperPlayServerTeams.ScoreBoardTeamInfo? = null

class PaperNametagPlayer(private val player: Player) : NametagPlayer {
    override val uuid: UUID get() = player.uniqueId
    override val name: String get() = player.name
    override val isOnline: Boolean get() = player.isOnline

    override fun showNametag(teamName: String, nametag: Nametag, config: NametagConfig) {
        player.sendPacket(
            createTeamPacket(
                teamName,
                WrapperPlayServerTeams.TeamMode.CREATE,
                nametag,
                config
            )
        )
    }

    override fun updateNametag(teamName: String, nametag: Nametag, config: NametagConfig) {
        player.sendPacket(
            createTeamPacket(
                teamName,
                WrapperPlayServerTeams.TeamMode.UPDATE,
                nametag,
                config
            )
        )
    }

    override fun hideNametag(teamName: String) {
        player.sendPacket(
            WrapperPlayServerTeams(
                teamName,
                WrapperPlayServerTeams.TeamMode.REMOVE,
                nullScoreboardInfo
            )
        )
    }

    private fun createTeamPacket(
        teamName: String,
        mode: WrapperPlayServerTeams.TeamMode,
        nameTag: Nametag,
        config: NametagConfig
    ) =
        WrapperPlayServerTeams(
            teamName,
            mode,
            WrapperPlayServerTeams.ScoreBoardTeamInfo(
                teamDisplayName(teamName),
                nameTag.prefix,
                nameTag.suffix,
                config.nameTagVisibility.toPacketEvents(),
                config.collisionRule.toPacketEvents(),
                config.nameColor,
                config.optionData.toPacketEvents()
            ),
            nameTag.playerName
        )
}
