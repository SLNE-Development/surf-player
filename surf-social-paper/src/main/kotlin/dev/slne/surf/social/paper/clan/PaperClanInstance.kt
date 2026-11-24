package dev.slne.surf.social.paper.clan

import com.google.auto.service.AutoService
import dev.slne.surf.social.paper.clan.commands.clanCommand
import dev.slne.surf.social.paper.clan.commands.subcommands.ClanChatCommand
import dev.slne.surf.social.paper.clan.listener.ClanTagColorJoinListener
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.social.core.common.clan.ClanInstance
import dev.slne.surf.social.core.client.clan.ClientClanInstance

@AutoService(ClanInstance::class)
class PaperClanInstance : ClientClanInstance() {
    private val log = logger()

    override val dataPath get() = plugin.dataPath

    override suspend fun enable() {
        super.enable()
        clanCommand()

        if (plugin.checkLuckPerms()) {
            ClanTagColorJoinListener.register(plugin)
        } else {
            log.atWarning()
                .log("LuckPerms is not enabled, clan tag colors will not be enforced")
        }

        if (plugin.checkSurfChat()) {
            ClanChatCommand("clanchat").register()
            ClanChatCommand("cc").register()
        }
    }
}