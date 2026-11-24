package dev.slne.surf.social.paper.shrieker

import dev.slne.surf.social.paper.PaperSocialFeature

/**
 * The former surf-shrieker Paper plugin had no Paper-side logic; this feature
 * keeps its slot in the lifecycle order.
 */
object PaperShriekerFeature : PaperSocialFeature {
    override val name = "shrieker"
}
