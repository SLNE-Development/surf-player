package dev.slne.surf.player.core.client.nametag.service

import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.player.api.nametag.nametag.Nametag
import dev.slne.surf.player.api.nametag.nametag.NametagVisibility
import dev.slne.surf.player.api.nametag.nametag.nametag
import dev.slne.surf.player.api.settings.SurfSettingsApi
import dev.slne.surf.player.api.settings.setting.SettingKeys
import dev.slne.surf.player.core.client.settings.service.SettingsService
import dev.slne.surf.player.core.client.contentcreator.LiveTagRenderer
import dev.slne.surf.player.core.client.nametag.config.NametagConfig
import dev.slne.surf.player.core.client.nametag.hook.ClanHook
import dev.slne.surf.player.core.client.nametag.platform.NametagPlatform
import dev.slne.surf.player.core.client.util.luckPermsPrefix
import dev.slne.surf.player.core.client.nametag.platform.NametagPlayer
import net.kyori.adventure.text.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object NametagService {
    private val preventNametagUpdateUuids = ConcurrentHashMap.newKeySet<UUID>()
    private val visibilityOverrides = ConcurrentHashMap<UUID, NametagVisibility>()

    private val clanTagSpacer by lazy { NametagPlatform.shift(4) }

    private fun isPrevented(player: NametagPlayer) = player.uuid in preventNametagUpdateUuids

    fun preventUpdates(uuid: UUID) {
        preventNametagUpdateUuids.add(uuid)
    }

    fun allowUpdates(uuid: UUID) {
        preventNametagUpdateUuids.remove(uuid)
    }

    /**
     * Shows the nametag of [uuid] with [visibility] to every viewer until [resetVisibility] or the
     * player leaves; viewers who already see it are updated asynchronously.
     */
    fun overrideVisibility(uuid: UUID, visibility: NametagVisibility) {
        if (visibilityOverrides.put(uuid, visibility) != visibility) refresh(uuid)
    }

    /**
     * Removes the visibility override of [uuid], if any, and updates every viewer asynchronously.
     */
    fun resetVisibility(uuid: UUID) {
        if (visibilityOverrides.remove(uuid) != null) refresh(uuid)
    }

    init {
        SettingsService.onPlayerSettingsChanged { viewerUuid, settingNames ->
            if (SettingKeys.SHOW_NAMETAGS.name in settingNames) refreshViewer(viewerUuid)
        }
    }

    /**
     * The config the nametag of [uuid] is rendered with: [config] with the visibility override applied.
     */
    fun configFor(uuid: UUID, config: NametagConfig): NametagConfig {
        return visibilityOverrides[uuid]?.let { config.copy(nameTagVisibility = it) } ?: config
    }

    /**
     * The config [viewerUuid] sees the nametag of [uuid] with: [configFor], hidden entirely if the
     * viewer turned off [SettingKeys.SHOW_NAMETAGS]. Every viewer sees each nametag through a team
     * of their own, so this only affects that viewer. Their own nametag is never hidden.
     */
    fun configFor(uuid: UUID, viewerUuid: UUID, config: NametagConfig): NametagConfig {
        val playerConfig = configFor(uuid, config)
        if (viewerUuid == uuid || showsNametags(viewerUuid)) return playerConfig

        return playerConfig.copy(nameTagVisibility = NametagVisibility.NEVER)
    }

    private fun showsNametags(viewerUuid: UUID) =
        SurfSettingsApi.getSettingValue(viewerUuid, SettingKeys.SHOW_NAMETAGS)

    private fun refresh(uuid: UUID) {
        val player = NametagPlatform.player(uuid) ?: return
        NametagPlatform.launch { handleDataUpdate(player) }
    }

    /** Re-renders every nametag [viewerUuid] sees, after their [SettingKeys.SHOW_NAMETAGS] changed. */
    private fun refreshViewer(viewerUuid: UUID) {
        val viewer = NametagPlatform.player(viewerUuid) ?: return

        NametagPlatform.launch {
            if (isPrevented(viewer)) return@launch
            val config = NametagConfig.getConfig()

            for (player in NametagPlatform.onlinePlayers()) {
                if (player.uuid == viewerUuid || isPrevented(player)) continue

                val nametag = createNameTag(player, clanTag(player))
                if (!viewer.isOnline) return@launch

                viewer.updateNametag(
                    teamName(player.name, viewer.name),
                    nametag,
                    configFor(player.uuid, viewerUuid, config)
                )
            }
        }
    }

    private suspend fun clanTag(player: NametagPlayer) = ClanHook.getClanTag(player.uuid)

    private fun createNameTag(player: NametagPlayer, clanTag: Component): Nametag {
        val playerUuid = player.uuid
        val prefixText = luckPermsPrefix(playerUuid)

        return nametag {
            prefix {
                if (prefixText.isNotEmpty()) {
                    append(miniMessage.deserialize(prefixText))
                }
            }

            playerName(player.name)

            suffix {
                spacer(clanTagSpacer)
                append(clanTag)
                appendSpace()
                append(LiveTagRenderer.render(playerUuid, space = true))
            }
        }
    }

    suspend fun handleJoin(player: NametagPlayer) {
        if (isPrevented(player) || !player.isOnline) {
            return
        }

        val playerUuid = player.uuid
        val playerName = player.name

        val playerNametag = createNameTag(player, clanTag(player))
        val config = NametagConfig.getConfig()

        for (viewer in NametagPlatform.onlinePlayers()) {
            if (!player.isOnline) {
                return
            }

            if (isPrevented(viewer)) {
                continue
            }

            val viewerName = viewer.name
            viewer.showNametag(
                teamName(playerName, viewerName),
                playerNametag,
                configFor(playerUuid, viewer.uuid, config)
            )

            if (viewer.uuid != playerUuid) {
                val viewerNametag = createNameTag(viewer, clanTag(viewer))

                if (viewer.isOnline) {
                    player.showNametag(
                        teamName(viewerName, playerName),
                        viewerNametag,
                        configFor(viewer.uuid, playerUuid, config)
                    )
                }
            }
        }
    }

    suspend fun handleJoin(player: NametagPlayer, viewer: NametagPlayer) {
        if (isPrevented(player) || isPrevented(viewer)) {
            return
        }

        val nameTag = createNameTag(player, clanTag(player))

        if (!player.isOnline) {
            return
        }

        viewer.showNametag(
            teamName(player.name, viewer.name),
            nameTag,
            configFor(player.uuid, viewer.uuid, NametagConfig.getConfig())
        )
    }

    suspend fun handleDataUpdate(player: NametagPlayer) {
        if (isPrevented(player)) {
            return
        }

        val nameTag = createNameTag(player, clanTag(player))

        if (!player.isOnline) {
            return
        }

        val config = NametagConfig.getConfig()
        val playerName = player.name

        for (viewer in NametagPlatform.onlinePlayers()) {
            if (isPrevented(viewer)) {
                continue
            }

            viewer.updateNametag(
                teamName(playerName, viewer.name),
                nameTag,
                configFor(player.uuid, viewer.uuid, config)
            )
        }
    }

    fun handleLeave(player: NametagPlayer) {
        if (!isPrevented(player)) {
            val playerName = player.name

            for (viewer in NametagPlatform.onlinePlayers()) {
                if (isPrevented(viewer)) {
                    continue
                }

                viewer.hideNametag(teamName(playerName, viewer.name))
            }
        }

        allowUpdates(player.uuid)
        visibilityOverrides.remove(player.uuid)
    }

    fun handleLeave(player: NametagPlayer, viewer: NametagPlayer) {
        if (isPrevented(player) || isPrevented(viewer)) {
            return
        }

        viewer.hideNametag(teamName(player.name, viewer.name))
    }
}

/**
 * The name of the team [viewerName] sees the nametag of [playerName] under.
 *
 * Nametags are scoped to a single viewer, so the pair of names has to be part of the team name.
 */
fun teamName(playerName: String, viewerName: String) = "$playerName-$viewerName"

/**
 * The display name [teamName] is registered under.
 */
fun teamDisplayName(teamName: String): Component = Component.text("surf-nametag-${teamName}")
