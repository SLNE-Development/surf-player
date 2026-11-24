package dev.slne.surf.player.core.client.settings.service

import dev.slne.surf.api.core.util.emptyObjectSet
import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.player.api.settings.setting.PlayerSetting
import dev.slne.surf.player.api.settings.setting.Setting
import dev.slne.surf.player.core.client.settings.rpc.settingsRpcService
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * The registered settings and the values of the players online on this server.
 *
 * Both are immutable snapshots that are replaced as a whole, so readers never see a half-updated
 * state.
 */
object SettingsService {
    private val registry = AtomicReference(SettingRegistry.EMPTY)
    private val playerCache = ConcurrentHashMap<UUID, PlayerSettings>()

    val settings: ObjectSet<Setting> get() = registry.get().all

    fun getSettingByName(name: String): Setting? = registry.get().byName[name]

    fun getSettingsForPlayer(playerUuid: UUID): ObjectSet<PlayerSetting> =
        playerCache[playerUuid]?.all ?: emptyObjectSet()

    fun getSettingForPlayer(playerUuid: UUID, settingName: String): PlayerSetting? =
        playerCache[playerUuid]?.byName?.get(settingName)

    fun getSettingForPlayerOrDefault(playerUuid: UUID, settingName: String): PlayerSetting? {
        getSettingForPlayer(playerUuid, settingName)?.let { return it }

        val setting = getSettingByName(settingName) ?: return null
        return PlayerSetting(setting = setting, settingValue = setting.defaultValue)
    }

    fun getSettingValueOrDefault(playerUuid: UUID, settingName: String): String? =
        getSettingForPlayer(playerUuid, settingName)?.settingValue
            ?: getSettingByName(settingName)?.defaultValue

    /**
     * Every registered setting with the value of [playerUuid], or its default value where they
     * chose none.
     */
    fun getLoadedSettingsWithDefaults(playerUuid: UUID): ObjectSet<PlayerSetting> {
        val cached = playerCache[playerUuid]?.byName
        val known = registry.get().byName
        val result = mutableObjectSetOf<PlayerSetting>(known.size)

        for (setting in known.values) {
            result.add(
                cached?.get(setting.name)
                    ?: PlayerSetting(setting = setting, settingValue = setting.defaultValue)
            )
        }

        return result.freeze()
    }

    /** Updates the cached value of [playerUuid], if their settings are cached. */
    fun cachePlayerSetting(playerUuid: UUID, playerSetting: PlayerSetting) {
        playerCache.computeIfPresent(playerUuid) { _, existing -> existing.with(playerSetting) }
    }

    suspend fun savePlayerSetting(playerUuid: UUID, playerSetting: PlayerSetting) {
        settingsRpcService.savePlayerSetting(
            playerUuid,
            playerSetting.setting.name,
            playerSetting.settingValue
        )
    }

    /** [cachePlayerSetting] followed by [savePlayerSetting]. */
    suspend fun cacheAndSavePlayerSetting(playerUuid: UUID, playerSetting: PlayerSetting) {
        cachePlayerSetting(playerUuid, playerSetting)
        savePlayerSetting(playerUuid, playerSetting)
    }

    /** Loads the values of [playerUuid] from the microservice into the cache. */
    suspend fun cachePlayerSettings(playerUuid: UUID) {
        val loaded = settingsRpcService.fetchPlayerSettings(playerUuid)

        val byName = mutableObject2ObjectMapOf<String, PlayerSetting>(loaded.size)
        val currentSettings = registry.get().byName
        for ((settingName, value) in loaded) {
            val setting = currentSettings[settingName] ?: continue
            byName[settingName] = PlayerSetting(setting = setting, settingValue = value)
        }

        playerCache[playerUuid] = PlayerSettings(byName)
    }

    /**
     * [cachePlayerSettings] for a player who just joined; drops the cache again if they already
     * left while it was loading, as their quit no longer clears it.
     */
    suspend fun cacheJoinedPlayerSettings(playerUuid: UUID, isOnline: () -> Boolean) {
        cachePlayerSettings(playerUuid)

        if (!isOnline()) {
            invalidatePlayerSettingsCache(playerUuid)
        }
    }

    fun invalidatePlayerSettingsCache(playerUuid: UUID) {
        playerCache.remove(playerUuid)
    }

    suspend fun refreshSettings() {
        val loaded = settingsRpcService.fetchSettings()

        val byName = mutableObject2ObjectMapOf<String, Setting>(loaded.size)
        for (setting in loaded) {
            byName[setting.name] = setting
        }

        registry.set(SettingRegistry(byName))
    }

    suspend fun createSetting(name: String, defaultValue: String): Setting {
        getSettingByName(name)?.let { return it }

        val created = settingsRpcService.createSetting(name, defaultValue)

        registry.updateAndGet { current ->
            if (current.byName.containsKey(name)) return@updateAndGet current

            val copy = mutableObject2ObjectMapOf<String, Setting>(current.byName.size + 1)
            copy.putAll(current.byName)
            copy[name] = created
            SettingRegistry(copy)
        }

        return created
    }
}

/** An immutable snapshot of the registered settings. */
private class SettingRegistry(val byName: Object2ObjectOpenHashMap<String, Setting>) {
    val all: ObjectSet<Setting> = mutableObjectSetOf<Setting>(byName.size)
        .apply { addAll(byName.values) }
        .freeze()

    companion object {
        val EMPTY = SettingRegistry(mutableObject2ObjectMapOf())
    }
}

/** An immutable snapshot of the values one player chose. */
private class PlayerSettings(val byName: Object2ObjectOpenHashMap<String, PlayerSetting>) {
    val all: ObjectSet<PlayerSetting> = mutableObjectSetOf<PlayerSetting>(byName.size)
        .apply { addAll(byName.values) }
        .freeze()

    fun with(playerSetting: PlayerSetting): PlayerSettings {
        val copy = mutableObject2ObjectMapOf<String, PlayerSetting>(byName.size + 1)
        copy.putAll(byName)
        copy[playerSetting.setting.name] = playerSetting
        return PlayerSettings(copy)
    }
}
