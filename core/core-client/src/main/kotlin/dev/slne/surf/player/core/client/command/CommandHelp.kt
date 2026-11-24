package dev.slne.surf.player.core.client.command

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

/**
 * One line of a command help: the full command line with its arguments ([usage], e.g.
 * `/friend add <spieler>`), a short German [description] and the [permission] a sender needs to
 * see the line (`null`: every sender who may use the root command).
 */
data class HelpEntry(
    val usage: String,
    val description: String,
    val permission: String? = null,
)

/**
 * The styled help of a root command, shown when the root is run without arguments.
 *
 * Every platform renders the same help from the same entries; only the lines the sender has the
 * permission for are shown.
 */
class CommandHelp(
    val title: String,
    val entries: List<HelpEntry>,
) {
    /**
     * Renders the help for a sender who holds exactly the permissions [hasPermission] returns
     * `true` for.
     */
    fun render(hasPermission: (String) -> Boolean): Component =
        renderHelp(title, entries.filter { entry -> entry.permission?.let(hasPermission) ?: true })

    companion object {
        /**
         * Renders [entries] under [title]: one line per entry with the usage (clicking it puts the
         * command into the chat) and its description.
         */
        fun renderHelp(title: String, entries: List<HelpEntry>): Component = buildText {
            appendInfoPrefix()
            primary(title.toSmallCaps(), TextDecoration.BOLD)

            if (entries.isEmpty()) {
                appendNewline()
                appendInfoPrefix()
                info(CommandMessages.NOTHING_AVAILABLE)
                return@buildText
            }

            for (entry in entries) {
                appendNewline()
                appendInfoPrefix()
                append(CommonComponents.EM_DASH)
                appendSpace()
                append {
                    variableKey(entry.usage)
                    hoverEvent(buildText { info("Klicke, um den Befehl einzufügen.") })
                    clickSuggestsCommand(suggestionOf(entry.usage))
                }
                spacer(" - ")
                info(entry.description)
            }
        }

        /**
         * The part of [usage] in front of the first argument placeholder, which is what clicking a
         * help line puts into the chat.
         */
        fun suggestionOf(usage: String): String {
            val argumentStart = usage.indexOfFirst { it == '<' || it == '[' }
            if (argumentStart < 0) return usage

            return usage.substring(0, argumentStart)
        }
    }
}
