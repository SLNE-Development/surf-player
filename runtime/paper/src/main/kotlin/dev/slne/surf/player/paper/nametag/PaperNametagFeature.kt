package dev.slne.surf.player.paper.nametag

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.player.core.client.nametag.command.NametagCommands
import dev.slne.surf.player.core.client.nametag.permission.NametagPermissions
import dev.slne.surf.player.paper.command.PaperAdminAction
import dev.slne.surf.player.core.client.nametag.config.NametagConfig
import dev.slne.surf.player.core.client.nametag.hook.ClanHook
import dev.slne.surf.player.core.client.nametag.hook.ContentCreatorHook
import dev.slne.surf.player.core.client.nametag.hook.LuckPermsHook
import dev.slne.surf.player.paper.PaperPlayerFeature
import dev.slne.surf.player.paper.hook.subscribeToLuckPermsUserChanges
import dev.slne.surf.player.paper.nametag.listener.NametagListener

/**
 * The former surf-nametag Paper plugin.
 *
 * Clans and content creators are bundled in the same plugin and enabled before this feature,
 * so their hooks are always registered.
 */
object PaperNametagFeature : PaperPlayerFeature {
    override val name = "nametag"

    override suspend fun onEnable() {
        NametagConfig.init()
        NametagListener.register()

        subscribeToLuckPermsUserChanges(LuckPermsHook::updatePlayer)

        ClanHook.createListeners()
        ContentCreatorHook.registerListener()
    }

    override fun adminActions() = listOf(
        PaperAdminAction("reload", NametagPermissions.ADMIN_RELOAD, "Lädt die Nametag-Konfiguration neu") {
            anyExecutor { sender, _ -> sender.sendMessage(NametagCommands.reload()) }
        },
    )
}
