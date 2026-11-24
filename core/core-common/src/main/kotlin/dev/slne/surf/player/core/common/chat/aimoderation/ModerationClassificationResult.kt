package dev.slne.surf.player.core.common.chat.aimoderation

import kotlinx.serialization.Serializable

@Serializable
data class ModerationClassificationResult(
    val action: ModerationClassificationAction,
    val flaggedScores: Map<ModerationCategory, Double>
)