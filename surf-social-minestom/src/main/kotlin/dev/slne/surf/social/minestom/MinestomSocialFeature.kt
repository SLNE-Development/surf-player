package dev.slne.surf.social.minestom

/**
 * One feature (chat, friends, clan, tab, ...) bundled into [SocialMinestomPlugin].
 *
 * The plugin forwards its own lifecycle to every feature: [onLoad] and [onEnable] in the order
 * the features are registered, [onDisable] in reverse order.
 */
interface MinestomSocialFeature {
    /**
     * Short identifier of the feature; also the name of its subfolder in the plugin's data
     * directory, see [SocialMinestomPlugin.featureDataDirectory].
     */
    val id: String

    suspend fun onLoad() = Unit

    suspend fun onEnable() = Unit

    suspend fun onDisable() = Unit
}
