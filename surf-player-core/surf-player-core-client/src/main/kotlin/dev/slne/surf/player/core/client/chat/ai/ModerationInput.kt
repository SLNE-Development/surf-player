package dev.slne.surf.player.core.client.chat.ai

/**
 * What a moderation score is looked up for; equal inputs share a cached score.
 */
internal data class ModerationInput(
    val model: String,
    val message: String,
    val messageType: String
)
