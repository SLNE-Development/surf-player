package dev.slne.surf.player.core.client.command

/**
 * The on/off argument every command uses (`[an|aus]`). German words are suggested; the English
 * ones are still accepted.
 */
object Toggle {
    val suggestions = listOf("an", "aus")

    const val INVALID = "Bitte gib 'an' oder 'aus' an."

    fun parse(input: String): Boolean? = when (input.lowercase()) {
        "an", "on", "enable", "true" -> true
        "aus", "off", "disable", "false" -> false
        else -> null
    }
}
