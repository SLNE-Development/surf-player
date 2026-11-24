package dev.slne.surf.player.core.client.command

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

/**
 * The root permission of the admin command (`/surfplayer`, `/surfplayervelocity`) and the scheme
 * of the permissions of its actions.
 */
object AdminPermissions {
    /** Needed to see the admin command at all. */
    const val ADMIN = "surf.player.admin"

    /** The permission of the admin action [action] of [feature]. */
    fun action(feature: String, action: String) = "$ADMIN.$feature.$action"
}

/**
 * What the help of the admin command shows about one action of a feature: `/<root> <feature>
 * <name> <arguments>` and its [description]. Only senders holding [permission] see it.
 */
data class AdminActionHelp(
    val name: String,
    val arguments: String,
    val description: String,
    val permission: String,
) {
    fun usage(root: String, feature: String): String =
        listOf("/$root", feature, name, arguments).filter { it.isNotBlank() }.joinToString(" ")
}

/**
 * Renders the help of the admin command, the same on every platform.
 */
object AdminCommandHelp {

    /**
     * The overview `/<root>` shows: every feature the sender may administrate with the actions they
     * may run in it.
     */
    fun overview(
        root: String,
        features: Map<String, List<AdminActionHelp>>,
        hasPermission: (String) -> Boolean,
    ): Component = buildText {
        appendInfoPrefix()
        primary("Administration".toSmallCaps(), TextDecoration.BOLD)

        val visible = features.mapValues { (_, actions) -> actions.filter { hasPermission(it.permission) } }
            .filterValues { it.isNotEmpty() }

        if (visible.isEmpty()) {
            appendNewline()
            appendInfoPrefix()
            info(CommandMessages.NOTHING_AVAILABLE)
            return@buildText
        }

        for ((feature, actions) in visible) {
            appendNewline()
            appendInfoPrefix()
            append(CommonComponents.EM_DASH)
            appendSpace()
            append {
                variableKey("/$root $feature")
                hoverEvent(buildText { info("Klicke, um die Aktionen anzuzeigen.") })
                clickRunsCommand("/$root $feature")
            }
            spacer(" - ")
            info(actions.joinToString(", ") { it.name })
        }
    }

    /**
     * The help `/<root> <feature>` shows: every action of [feature] the sender may run.
     */
    fun feature(
        root: String,
        feature: String,
        actions: List<AdminActionHelp>,
        hasPermission: (String) -> Boolean,
    ): Component = CommandHelp.renderHelp(
        "/$root $feature",
        actions.filter { hasPermission(it.permission) }
            .map { HelpEntry(it.usage(root, feature), it.description, it.permission) }
    )
}
