package dev.slne.surf.player.core.common.settings.rpc

import dev.slne.surf.player.api.settings.setting.Setting
import dev.slne.surf.rabbitmq.api.rpc.RpcService
import java.util.*

/**
 * The registered settings and the values players chose for them, stored by the microservice.
 */
@RpcService
interface SettingsRpcService {
    /** Every registered setting. */
    suspend fun fetchSettings(): List<Setting>

    /** Registers the setting [name], or replaces its default value if it already exists. */
    suspend fun createSetting(name: String, defaultValue: String): Setting

    /** The values [playerUuid] chose, by setting name. Settings left at their default are missing. */
    suspend fun fetchPlayerSettings(playerUuid: UUID): Map<String, String>

    /** Stores [value] as the value [playerUuid] chose for the setting [settingName]. */
    suspend fun savePlayerSetting(playerUuid: UUID, settingName: String, value: String): Boolean
}
