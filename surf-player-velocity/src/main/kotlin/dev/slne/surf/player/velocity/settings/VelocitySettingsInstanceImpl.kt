package dev.slne.surf.social.velocity.settings

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.client.settings.ClientLoader
import dev.slne.surf.social.core.client.settings.ClientSettingsInstance
import dev.slne.surf.social.core.common.settings.SettingsInstance

@AutoService(SettingsInstance::class)
class VelocitySettingsInstanceImpl : ClientSettingsInstance {
    override val clientLoader = ClientLoader(plugin.dataPath)
}