package dev.slne.surf.player.core.client.minimap.config

import dev.slne.surf.api.core.config.SpongeConfigClass
import java.io.UncheckedIOException
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * A config file a reload reads again with [load], and puts back with [restore] when a later file fails.
 */
class ReloadableConfig<C>(
    val fileName: String,
    val load: () -> C,
    val current: () -> C,
    val restore: (C) -> Unit,
)

private fun <C> SpongeConfigClass<C>.reloadable(fileName: String) =
    ReloadableConfig(fileName, ::reloadFromFile, ::getConfig) { manager.config = it }

/**
 * The outcome of [CompanionConfigs.reload].
 */
sealed interface ReloadResult {
    val duration: Duration

    data class Reloaded(val settings: CompanionSettings, override val duration: Duration) : ReloadResult {
        val config get() = settings.config
        val globalSettings get() = settings.global
        val xaero get() = settings.xaero
        val profile get() = settings.xaeroProfile
        val journeyMap get() = settings.journeyMap
        val journeyMapProfile get() = settings.journeyMapProfile
        val voxelMap get() = settings.voxelMap
        val voxelMapProfile get() = settings.voxelMapProfile
    }

    /**
     * [fileName] could not be read; every file keeps the config it had before.
     */
    data class Failed(val fileName: String, val reason: String, override val duration: Duration) : ReloadResult
}

/**
 * Loads the companion's config files: `config.yml` for every map mod, `xaero.yml` for Xaero's mods,
 * `journeymap.yml` for JourneyMap and `voxelmap.yml` for VoxelMap.
 */
object CompanionConfigs {

    @Volatile
    private var settings: CompanionSettings? = null

    /**
     * The settings every sync reads: all four files as they were last loaded together.
     */
    val current: CompanionSettings
        get() = settings ?: error("The companion's config files are not loaded yet")

    /**
     * Loads every file and checks the global settings and the options of the map mods, so their warnings and the
     * options the global settings override show up at startup.
     */
    fun init() {
        CompanionConfig.init()
        CompanionXaeroConfig.init()
        CompanionJourneyMapConfig.init()
        CompanionVoxelMapConfig.init()
        settings = CompanionSettings(
            CompanionConfig.getConfig(),
            CompanionXaeroConfig.getConfig(),
            CompanionJourneyMapConfig.getConfig(),
            CompanionVoxelMapConfig.getConfig(),
        )
    }

    /**
     * Reads every file again and checks the global settings and the options of the map mods, which logs their
     * warnings. Only once every file loaded do the new settings become [current]; the platforms then sync every
     * player again, since the reloaded files can change the level IDs and what each mod enforces. One reload runs
     * at a time.
     */
    @Synchronized
    fun reload(
        main: ReloadableConfig<MinimapCompanionConfig> = CompanionConfig.reloadable("config.yml"),
        xaero: ReloadableConfig<XaeroConfig> = CompanionXaeroConfig.reloadable("xaero.yml"),
        journeyMap: ReloadableConfig<JourneyMapConfig> = CompanionJourneyMapConfig.reloadable("journeymap.yml"),
        voxelMap: ReloadableConfig<VoxelMapConfig> = CompanionVoxelMapConfig.reloadable("voxelmap.yml"),
    ): ReloadResult {
        val start = TimeSource.Monotonic.markNow()
        val reload = FileReload()

        return try {
            val config = reload.load(main)
            val xaeroConfig = reload.load(xaero)
            val journeyMapConfig = reload.load(journeyMap)
            val voxelMapConfig = reload.load(voxelMap)
            val loaded = CompanionSettings(config, xaeroConfig, journeyMapConfig, voxelMapConfig)
            settings = loaded
            ReloadResult.Reloaded(loaded, start.elapsedNow())
        } catch (e: FileFailed) {
            reload.restore()
            // The config manager already logged the cause; the admin only needs to know what broke.
            val reason = e.cause.cause?.message ?: e.cause.message ?: e.cause.javaClass.simpleName
            ReloadResult.Failed(e.file.fileName, reason, start.elapsedNow())
        }
    }
}

/**
 * Loads files one after another and remembers how to put back the ones already loaded.
 */
private class FileReload {
    private val restores = mutableListOf<() -> Unit>()

    fun <C> load(file: ReloadableConfig<C>): C {
        val previous = file.current()
        val loaded = try {
            file.load()
        } catch (e: UncheckedIOException) {
            throw FileFailed(file, e)
        }
        restores += { file.restore(previous) }
        return loaded
    }

    fun restore() = restores.forEach { it() }
}

private class FileFailed(val file: ReloadableConfig<*>, override val cause: UncheckedIOException) : Exception(cause)
