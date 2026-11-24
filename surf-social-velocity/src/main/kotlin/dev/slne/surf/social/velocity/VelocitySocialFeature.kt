package dev.slne.surf.social.velocity

/**
 * One feature (currently only clan) bundled into [SocialVelocityPlugin].
 *
 * The plugin forwards its lifecycle to every feature: [onLoad] (plugin construction) and
 * [onEnable] (proxy initialization) in registration order, [onDisable] (proxy shutdown) in
 * reverse order.
 */
interface VelocitySocialFeature {
    /**
     * Short identifier of the feature; also the name of its subfolder in the plugin's data
     * directory, see [SocialVelocityPlugin.featureDataDirectory].
     */
    val id: String

    suspend fun onLoad() = Unit

    suspend fun onEnable() = Unit

    suspend fun onDisable() = Unit
}
