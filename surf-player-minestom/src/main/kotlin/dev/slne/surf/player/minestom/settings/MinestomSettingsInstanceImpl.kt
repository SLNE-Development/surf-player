package dev.slne.surf.social.minestom.settings

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.client.settings.ClientLoader
import dev.slne.surf.social.core.client.settings.ClientSettingsInstance
import dev.slne.surf.social.core.common.settings.SettingsInstance
import net.kyori.adventure.util.Services

@AutoService(SettingsInstance::class)
class MinestomSettingsInstanceImpl : ClientSettingsInstance, Services.Fallback {
    override val clientLoader = ClientLoader(SurfSettingsMinestomPlugin.dataDirectory)
}
