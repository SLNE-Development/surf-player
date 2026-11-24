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
    suspend fun findAllSettings(): List<Setting>

    /**
     * Registers every setting of [defaultValues] (setting name to default value) in one
     * transaction, replacing the default value of settings that already exist.
     */
    suspend fun createSettings(defaultValues: Map<String, String>): List<Setting>

    /** The values [playerUuid] chose, by setting name. Settings left at their default are missing. */
    suspend fun findPlayerSettings(playerUuid: UUID): Map<String, String>

    /** Stores [value] as the value [playerUuid] chose for the setting [settingName]. */
    suspend fun savePlayerSetting(playerUuid: UUID, settingName: String, value: String)
}
