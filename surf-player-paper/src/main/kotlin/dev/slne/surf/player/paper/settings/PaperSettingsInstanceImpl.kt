package dev.slne.surf.social.paper.settings

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.common.settings.SettingsInstance
import dev.slne.surf.social.core.client.settings.ClientLoader
import dev.slne.surf.social.core.client.settings.ClientSettingsInstance
import net.kyori.adventure.util.Services

@AutoService(SettingsInstance::class)
class PaperSettingsInstanceImpl : ClientSettingsInstance, Services.Fallback {
    override val clientLoader = ClientLoader(plugin.dataPath)
}
