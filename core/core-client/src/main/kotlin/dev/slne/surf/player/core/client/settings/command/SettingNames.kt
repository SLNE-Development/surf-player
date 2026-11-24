package dev.slne.surf.player.core.client.settings.command

/**
 * Resolves what a player typed as setting argument to a registered setting name.
 *
 * Setting names are namespaced keys as built by `SettingKeys` (`feature:name`, e.g. `chat:pings`).
 * Matching ignores case and surrounding whitespace. A name without namespace (`pings`) resolves
 * when exactly one registered setting has that name in any namespace.
 */
object SettingNames {
    private const val SEPARATOR = ':'

    /** The registered name of [names] that [input] means, or `null` if none or several match. */
    fun resolve(input: String, names: Collection<String>): String? {
        val wanted = input.trim()
        if (wanted.isEmpty()) return null

        names.firstOrNull { it.equals(wanted, ignoreCase = true) }?.let { return it }
        if (SEPARATOR in wanted) return null

        return names.singleOrNull { it.substringAfter(SEPARATOR).equals(wanted, ignoreCase = true) }
    }

    /**
     * The names of [names] to suggest for [input]: those whose full name or name without namespace
     * starts with [input] (ignoring case), sorted.
     */
    fun suggestions(input: String, names: Collection<String>): List<String> {
        val prefix = input.trim()
        return names
            .filter {
                it.startsWith(prefix, ignoreCase = true) ||
                        it.substringAfter(SEPARATOR).startsWith(prefix, ignoreCase = true)
            }
            .sorted()
    }
}
