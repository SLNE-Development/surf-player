package dev.slne.surf.player.core.client.minimap.global

/**
 * Applies global entries to one option map of a mod file, as [raw] holds it, and reports through [hint] each
 * option whose configured value an entry replaces. [section] names the map inside [file], if the file has several.
 */
class OptionOverlay(
    private val file: String,
    private val section: String?,
    private val raw: Map<String, Any?>,
    private val hint: (String) -> Unit,
) {
    private val values = LinkedHashMap(raw)

    /**
     * Enforces [value] for option [id], as [feature]'s entry says.
     */
    fun set(feature: GlobalFeature, id: String, value: Any) {
        val configured = raw[id]
        if (configured != null && !configured.toString().equals(value.toString(), ignoreCase = true)) {
            report(feature, id)
        }
        values[id] = value
    }

    /**
     * Stops enforcing option [id], as [feature]'s entry says, so the player's own setting applies.
     */
    fun lift(feature: GlobalFeature, id: String) {
        if (raw[id] != null) report(feature, id)
        values.remove(id)
    }

    fun result(): Map<String, Any?> = values

    private fun report(feature: GlobalFeature, id: String) {
        val option = if (section == null) id else "$section.$id"
        hint("global-settings.${feature.key} overrides $option in $file")
    }
}
