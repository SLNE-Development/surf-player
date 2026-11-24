package dev.slne.surf.player.core.client.settings.service

import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.player.api.settings.setting.PlayerSetting
import dev.slne.surf.player.api.settings.setting.Setting
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.settings.redis.PlayerSettingChangedRedisEvent
import dev.slne.surf.player.core.client.settings.rpc.settingsRpcService
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
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
    private val changeListeners = CopyOnWriteArrayList<(playerUuid: UUID, settingNames: Set<String>) -> Unit>()

    /**
     * Calls [listener] whenever cached values of a player on this server change: with the changed
     * setting's name when one is changed, and with every non-default setting once their values are
     * loaded on join (their values were the defaults until then).
     */
    fun onPlayerSettingsChanged(listener: (playerUuid: UUID, settingNames: Set<String>) -> Unit) {
        changeListeners.add(listener)
    }

    private fun notifyChanged(playerUuid: UUID, settingNames: Set<String>) {
        if (settingNames.isEmpty()) return
        for (listener in changeListeners) {
            listener(playerUuid, settingNames)
        }
    }

    val settings: ObjectSet<Setting> get() = registry.get().all

    fun getSettingByName(name: String): Setting? = registry.get().byName[name]

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

    /**
     * Updates the cached value of [playerUuid], if their settings are cached and the value differs,
     * and notifies the [onPlayerSettingsChanged] listeners.
     */
    fun cachePlayerSetting(playerUuid: UUID, playerSetting: PlayerSetting) {
        val name = playerSetting.setting.name
        var changed = false

        playerCache.computeIfPresent(playerUuid) { _, existing ->
            if (existing.byName[name]?.settingValue == playerSetting.settingValue) {
                existing
            } else {
                changed = true
                existing.with(playerSetting)
            }
        }

        if (changed) notifyChanged(playerUuid, setOf(name))
    }

    /**
     * Applies a value of [playerUuid] that was saved on any server (see
     * [PlayerSettingChangedRedisEvent]) to this server's cache, if their settings are cached here.
     */
    fun applySavedSetting(playerUuid: UUID, settingName: String, value: String) {
        val setting = getSettingByName(settingName) ?: return
        cachePlayerSetting(playerUuid, PlayerSetting(setting = setting, settingValue = value))
    }

    /**
     * Stores the value and tells every server, so the caches of the servers (and the proxy) that
     * have the settings of [playerUuid] cached are updated too.
     */
    suspend fun savePlayerSetting(playerUuid: UUID, playerSetting: PlayerSetting) {
        val name = playerSetting.setting.name
        val value = playerSetting.settingValue

        settingsRpcService.savePlayerSetting(playerUuid, name, value)
        SurfPlayerClient.redisApi.publishEvent(PlayerSettingChangedRedisEvent(playerUuid, name, value)).await()
    }

    /** [cachePlayerSetting] followed by [savePlayerSetting]. */
    suspend fun cacheAndSavePlayerSetting(playerUuid: UUID, playerSetting: PlayerSetting) {
        cachePlayerSetting(playerUuid, playerSetting)
        savePlayerSetting(playerUuid, playerSetting)
    }

    /** Whether the values of [playerUuid] are cached on this server. */
    fun isCached(playerUuid: UUID): Boolean = playerCache.containsKey(playerUuid)

    /**
     * The value of [settingName] for [playerUuid], loaded from the microservice without caching it,
     * or the setting's default value; `null` if the setting is not registered.
     */
    suspend fun loadSettingValueOrDefault(playerUuid: UUID, settingName: String): String? =
        settingsRpcService.findPlayerSettings(playerUuid)[settingName]
            ?: getSettingByName(settingName)?.defaultValue

    /** Loads the values of [playerUuid] from the microservice into the cache. */
    suspend fun cachePlayerSettings(playerUuid: UUID) {
        val loaded = settingsRpcService.findPlayerSettings(playerUuid)

        val byName = mutableObject2ObjectMapOf<String, PlayerSetting>(loaded.size)
        val currentSettings = registry.get().byName
        for ((settingName, value) in loaded) {
            val setting = currentSettings[settingName] ?: continue
            byName[settingName] = PlayerSetting(setting = setting, settingValue = value)
        }

        playerCache[playerUuid] = PlayerSettings(byName)
        notifyChanged(playerUuid, byName.keys.toSet())
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
        val loaded = settingsRpcService.findAllSettings()

        val byName = mutableObject2ObjectMapOf<String, Setting>(loaded.size)
        for (setting in loaded) {
            byName[setting.name] = setting
        }

        registry.set(SettingRegistry(byName))
    }

    suspend fun createSetting(name: String, defaultValue: String): Setting =
        createSettings(mapOf(name to defaultValue)).getValue(name)

    /**
     * Registers the settings of [defaultValues] (setting name to default value) that are not
     * registered yet, with one call to the microservice; returns every requested setting by name.
     */
    suspend fun createSettings(defaultValues: Map<String, String>): Map<String, Setting> {
        val known = registry.get().byName
        val missing = defaultValues.filterKeys { it !in known }

        if (missing.isNotEmpty()) {
            val created = settingsRpcService.createSettings(missing)

            registry.updateAndGet { current ->
                val copy = mutableObject2ObjectMapOf<String, Setting>(current.byName.size + created.size)
                copy.putAll(current.byName)
                for (setting in created) {
                    copy.putIfAbsent(setting.name, setting)
                }
                SettingRegistry(copy)
            }
        }

        val byName = registry.get().byName
        return defaultValues.keys.associateWith { byName.getValue(it) }
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
