package dev.slne.surf.player.velocity.settings

import com.google.auto.service.AutoService
import dev.slne.surf.player.core.client.settings.ClientLoader
import dev.slne.surf.player.core.client.settings.ClientSettingsInstance
import dev.slne.surf.player.core.common.settings.SettingsInstance

@AutoService(SettingsInstance::class)
class VelocitySettingsInstanceImpl : ClientSettingsInstance {
    override val clientLoader = ClientLoader(plugin.dataPath)
}