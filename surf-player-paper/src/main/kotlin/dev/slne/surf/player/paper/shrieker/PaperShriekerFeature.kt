package dev.slne.surf.player.paper.shrieker

import dev.slne.surf.player.paper.PaperPlayerFeature

/**
 * The former surf-shrieker Paper plugin had no Paper-side logic; this feature
 * keeps its slot in the lifecycle order.
 */
object PaperShriekerFeature : PaperPlayerFeature {
    override val name = "shrieker"
}
