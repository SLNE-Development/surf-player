package dev.slne.surf.minimapcompanion.core.config

import dev.slne.surf.api.core.config.SpongeConfigClass
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
 * Loads the companion's config files: `config.yml` for every map mod and `xaero.yml` for Xaero's mods.
 */
object CompanionConfigs {

    sealed interface ReloadResult {
        val duration: Duration

        data class Reloaded(
            val config: MinimapCompanionConfig,
            val xaero: XaeroConfig,
            val profile: XaeroServerProfile,
            override val duration: Duration,
        ) : ReloadResult

        /**
         * [fileName] could not be read; every file keeps the config it had before.
         */
        data class Failed(val fileName: String, val reason: String, override val duration: Duration) : ReloadResult
    }

    /**
     * Loads both files and checks the Xaero options, so their warnings show up at startup.
     */
    fun init() {
        CompanionConfig.init()
        CompanionXaeroConfig.init()
        XaeroServerProfile.of(CompanionXaeroConfig.getConfig())
    }

    /**
     * Reads both files again and checks the Xaero options, which logs their warnings. The platforms then sync
     * every player again, since the reloaded files can change the level IDs, the rules and the enforced options.
     */
    fun reload(
        main: ReloadableConfig<MinimapCompanionConfig> = CompanionConfig.reloadable("config.yml"),
        xaero: ReloadableConfig<XaeroConfig> = CompanionXaeroConfig.reloadable("xaero.yml"),
    ): ReloadResult {
        val start = TimeSource.Monotonic.markNow()
        val previousMain = main.current()

        val config = try {
            main.load()
        } catch (e: UncheckedIOException) {
            return failed(main, e, start.elapsedNow())
        }

        val xaeroConfig = try {
            xaero.load()
        } catch (e: UncheckedIOException) {
            main.restore(previousMain)
            return failed(xaero, e, start.elapsedNow())
        }

        return ReloadResult.Reloaded(config, xaeroConfig, XaeroServerProfile.of(xaeroConfig), start.elapsedNow())
    }

    // The config manager already logged the cause; the admin only needs to know what broke.
    private fun failed(file: ReloadableConfig<*>, e: UncheckedIOException, duration: Duration) =
        ReloadResult.Failed(file.fileName, e.cause?.message ?: e.message ?: e.javaClass.simpleName, duration)
}
