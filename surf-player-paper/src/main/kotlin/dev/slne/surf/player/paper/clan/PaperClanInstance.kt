package dev.slne.surf.social.paper.clan

import com.google.auto.service.AutoService
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.social.core.common.clan.ClanInstance
import dev.slne.surf.social.paper.clan.commands.clanCommand
import dev.slne.surf.social.paper.clan.commands.subcommands.ClanChatCommand
import dev.slne.surf.social.paper.clan.listener.ClanTagColorJoinListener
import dev.slne.surf.social.paper.plugin

@AutoService(ClanInstance::class)
class PaperClanInstance : ClanInstance {
    override val dataPath get() = PaperClanFeature.dataPath

    override suspend fun enable() {
        super.enable()
        clanCommand()

        ClanTagColorJoinListener.register(plugin)

        ClanChatCommand("clanchat").register()
        ClanChatCommand("cc").register()
    }
}
