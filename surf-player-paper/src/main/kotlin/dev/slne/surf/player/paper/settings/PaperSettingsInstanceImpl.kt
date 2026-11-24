package dev.slne.surf.player.paper.settings

import com.google.auto.service.AutoService
import dev.slne.surf.player.core.common.settings.SettingsInstance
import dev.slne.surf.player.core.client.settings.ClientLoader
import dev.slne.surf.player.core.client.settings.ClientSettingsInstance
import net.kyori.adventure.util.Services

@AutoService(SettingsInstance::class)
class PaperSettingsInstanceImpl : ClientSettingsInstance, Services.Fallback {
    override val clientLoader = ClientLoader(plugin.dataPath)
}
