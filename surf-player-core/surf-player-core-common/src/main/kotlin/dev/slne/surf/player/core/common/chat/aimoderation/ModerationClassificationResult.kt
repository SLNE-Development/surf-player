package dev.slne.surf.social.core.common.chat.aimoderation

import kotlinx.serialization.Serializable

@Serializable
data class ModerationClassificationResult(
    val action: ModerationClassificationAction,
    val flaggedScores: Map<ModerationCategory, Double>
)