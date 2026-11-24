package dev.slne.surf.social.paper.clan

import com.google.auto.service.AutoService
import dev.slne.surf.social.paper.clan.commands.clanCommand
import dev.slne.surf.social.paper.clan.commands.subcommands.ClanChatCommand
import dev.slne.surf.social.paper.clan.listener.ClanTagColorJoinListener
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.social.core.common.clan.ClanInstance
import dev.slne.surf.social.core.client.clan.ClientClanInstance
import dev.slne.surf.social.paper.plugin

@AutoService(ClanInstance::class)
class PaperClanInstance : ClientClanInstance() {
    private val log = logger()

    override val dataPath get() = PaperClanFeature.dataPath

    override suspend fun enable() {
        super.enable()
        clanCommand()

        if (PaperClanFeature.checkLuckPerms()) {
            ClanTagColorJoinListener.register(plugin)
        } else {
            log.atWarning()
                .log("LuckPerms is not enabled, clan tag colors will not be enforced")
        }

        // Chat is bundled in the same plugin, so the clan chat commands are always available.
        ClanChatCommand("clanchat").register()
        ClanChatCommand("cc").register()
    }
}
