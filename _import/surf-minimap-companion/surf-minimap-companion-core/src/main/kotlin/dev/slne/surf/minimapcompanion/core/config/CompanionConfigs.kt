package dev.slne.surf.minimapcompanion.core.config

import dev.slne.surf.api.core.config.SpongeConfigClass
import dev.slne.surf.minimapcompanion.core.global.GlobalSettings
import dev.slne.surf.minimapcompanion.core.journeymap.JourneyMapProfile
import dev.slne.surf.minimapcompanion.core.voxelmap.VoxelMapProfile
import dev.slne.surf.minimapcompanion.core.xaero.XaeroServerProfile
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
 * Loads the companion's config files: `config.yml` for every map mod, `xaero.yml` for Xaero's mods,
 * `journeymap.yml` for JourneyMap and `voxelmap.yml` for VoxelMap.
 */
object CompanionConfigs {

    sealed interface ReloadResult {
        val duration: Duration

        data class Reloaded(
            val config: MinimapCompanionConfig,
            val globalSettings: GlobalSettings,
            val xaero: XaeroConfig,
            val profile: XaeroServerProfile,
            val journeyMap: JourneyMapConfig,
            val journeyMapProfile: JourneyMapProfile,
            val voxelMap: VoxelMapConfig,
            val voxelMapProfile: VoxelMapProfile,
            override val duration: Duration,
        ) : ReloadResult

        /**
         * [fileName] could not be read; every file keeps the config it had before.
         */
        data class Failed(val fileName: String, val reason: String, override val duration: Duration) : ReloadResult
    }

    /**
     * Loads every file and checks the global settings and the options of the map mods, so their warnings and the
     * options the global settings override show up at startup.
     */
    fun init() {
        CompanionConfig.init()
        CompanionXaeroConfig.init()
        CompanionJourneyMapConfig.init()
        CompanionVoxelMapConfig.init()
        val global = GlobalSettings.of(CompanionConfig.getConfig().globalSettings)
        XaeroServerProfile.of(CompanionXaeroConfig.getConfig(), global)
        JourneyMapProfile.of(CompanionJourneyMapConfig.getConfig(), global)
        VoxelMapProfile.of(CompanionVoxelMapConfig.getConfig(), global)
    }

    /**
     * Reads every file again and checks the options of the map mods, which logs their warnings. The platforms
     * then sync every player again, since the reloaded files can change the level IDs and what each mod enforces.
     */
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
            val global = GlobalSettings.of(config.globalSettings)
            ReloadResult.Reloaded(
                config,
                global,
                xaeroConfig,
                XaeroServerProfile.of(xaeroConfig, global),
                journeyMapConfig,
                JourneyMapProfile.of(journeyMapConfig, global),
                voxelMapConfig,
                VoxelMapProfile.of(voxelMapConfig, global),
                start.elapsedNow(),
            )
        } catch (e: FileFailed) {
            reload.restore()
            // The config manager already logged the cause; the admin only needs to know what broke.
            val reason = e.cause.cause?.message ?: e.cause.message ?: e.cause.javaClass.simpleName
            ReloadResult.Failed(e.file.fileName, reason, start.elapsedNow())
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
}
