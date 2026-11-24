package dev.slne.surf.social.core.client.chat.ai

/**
 * What a moderation score is looked up for; equal inputs share a cached score.
 */
internal data class ModerationInput(
    val model: String,
    val message: String,
    val messageType: String
)
