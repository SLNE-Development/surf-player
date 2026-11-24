package dev.slne.surf.player.core.client.settings.rpc

import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.common.settings.rpc.SettingsRpcService

val settingsRpcService by lazy { SurfPlayerClient.rabbitApi.createRpcService<SettingsRpcService>() }
