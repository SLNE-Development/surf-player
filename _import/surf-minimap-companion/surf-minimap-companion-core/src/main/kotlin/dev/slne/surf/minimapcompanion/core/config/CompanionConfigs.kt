package dev.slne.surf.minimapcompanion.core.config

import dev.slne.surf.api.core.config.SpongeConfigClass
import dev.slne.surf.minimapcompanion.core.journeymap.JourneyMapProfile
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
 * Loads the companion's config files: `config.yml` for every map mod, `xaero.yml` for Xaero's mods and
 * `journeymap.yml` for JourneyMap.
 */
object CompanionConfigs {

    sealed interface ReloadResult {
        val duration: Duration

        data class Reloaded(
            val config: MinimapCompanionConfig,
            val xaero: XaeroConfig,
            val profile: XaeroServerProfile,
            val journeyMap: JourneyMapConfig,
            val journeyMapProfile: JourneyMapProfile,
            override val duration: Duration,
        ) : ReloadResult

        /**
         * [fileName] could not be read; every file keeps the config it had before.
         */
        data class Failed(val fileName: String, val reason: String, override val duration: Duration) : ReloadResult
    }

    /**
     * Loads every file and checks the options of the map mods, so their warnings show up at startup.
     */
    fun init() {
        CompanionConfig.init()
        CompanionXaeroConfig.init()
        CompanionJourneyMapConfig.init()
        XaeroServerProfile.of(CompanionXaeroConfig.getConfig())
        JourneyMapProfile.of(CompanionJourneyMapConfig.getConfig())
    }

    /**
     * Reads every file again and checks the options of the map mods, which logs their warnings. The platforms
     * then sync every player again, since the reloaded files can change the level IDs and what each mod enforces.
     */
    fun reload(
        main: ReloadableConfig<MinimapCompanionConfig> = CompanionConfig.reloadable("config.yml"),
        xaero: ReloadableConfig<XaeroConfig> = CompanionXaeroConfig.reloadable("xaero.yml"),
        journeyMap: ReloadableConfig<JourneyMapConfig> = CompanionJourneyMapConfig.reloadable("journeymap.yml"),
    ): ReloadResult {
        val start = TimeSource.Monotonic.markNow()
        val reload = FileReload()

        return try {
            val config = reload.load(main)
            val xaeroConfig = reload.load(xaero)
            val journeyMapConfig = reload.load(journeyMap)
            ReloadResult.Reloaded(
                config,
                xaeroConfig,
                XaeroServerProfile.of(xaeroConfig),
                journeyMapConfig,
                JourneyMapProfile.of(journeyMapConfig),
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
