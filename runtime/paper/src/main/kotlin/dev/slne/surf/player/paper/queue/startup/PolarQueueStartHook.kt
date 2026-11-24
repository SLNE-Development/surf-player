package dev.slne.surf.player.paper.queue.startup

import top.polar.api.loader.LoaderApi

/**
 * Delays accepting queued players until the external PolarLoader plugin has finished loading.
 * Only touched when PolarLoader is installed, as it references Polar's classes.
 */
object PolarQueueStartHook {
    fun onPolarEnabled(callback: () -> Unit) {
        LoaderApi.registerEnableCallback { callback() }
    }
}
