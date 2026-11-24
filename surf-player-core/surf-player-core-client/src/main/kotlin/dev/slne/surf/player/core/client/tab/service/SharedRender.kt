package dev.slne.surf.player.core.client.tab.service

import net.kyori.adventure.text.Component

/**
 * An audience independent render of a template, together with the [values] it was built from.
 */
internal class SharedRender(val values: TablistValues, val component: Component)
