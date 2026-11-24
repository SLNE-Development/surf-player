package dev.slne.surf.player.tests.infra

/** Minimal line-based YAML editing for plugin configs (keeps comments and formatting). */
object Yaml {
    private val keyLine = Regex("""^(\s*)([A-Za-z0-9_.\-]+):(\s*)(.*)$""")

    /** Replaces the scalar value of the dotted [path] (e.g. "autoDisabling.enabled"); fails if absent. */
    fun set(text: String, path: String, value: String): String {
        val wanted = path.split('.')
        val stack = ArrayList<Pair<Int, String>>()
        val lines = text.lines().toMutableList()
        for (i in lines.indices) {
            val line = lines[i]
            if (line.isBlank() || line.trimStart().startsWith("#") || line.trimStart().startsWith("- ")) continue
            val m = keyLine.find(line) ?: continue
            val indent = m.groupValues[1].length
            while (stack.isNotEmpty() && stack.last().first >= indent) stack.removeAt(stack.size - 1)
            stack += indent to m.groupValues[2]
            if (stack.map { it.second } == wanted) {
                lines[i] = "${m.groupValues[1]}${m.groupValues[2]}: $value"
                return lines.joinToString("\n")
            }
        }
        throw IllegalArgumentException("YAML key $path not found")
    }

    /** Adds [item] as first entry of the list at top-level key [key]. */
    fun addListItem(text: String, key: String, item: String): String {
        val lines = text.lines().toMutableList()
        val idx = lines.indexOfFirst { it.startsWith("$key:") }
        require(idx >= 0) { "YAML list $key not found" }
        val indent = lines.drop(idx + 1).firstOrNull { it.trimStart().startsWith("- ") }?.takeWhile { it == ' ' } ?: "    "
        lines.add(idx + 1, "$indent- $item")
        return lines.joinToString("\n")
    }
}
