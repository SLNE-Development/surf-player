package dev.slne.surf.social.paper.shrieker

import dev.slne.surf.social.paper.PaperFeature

/**
 * The former surf-shrieker Paper plugin had no Paper-side logic; this feature
 * keeps its slot in the lifecycle order.
 */
object PaperShriekerFeature : PaperFeature {
    override val name = "shrieker"
}
