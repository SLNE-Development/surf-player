package dev.slne.surf.social.core.client.chat.util

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import java.net.URI

/**
 * These milliseconds, colored green below [good], yellow below [okay] and red otherwise.
 */
fun Long.coloredComponent(good: Long = 200L, okay: Long = 1000L): Component {
    val color = when {
        this < good -> Colors.GREEN
        this < okay -> Colors.YELLOW
        else -> Colors.RED
    }

    return buildText { append(Component.text("${this@coloredComponent}ms", color)) }
}

private val linkRegex = Regex("(?i)\\b((https?://)?[\\w-]+(\\.[\\w-]+)+(/\\S*)?)\\b")

fun updateLinks(rawMessage: Component): Component {
    var message = rawMessage
    val text = rawMessage.plain()

    linkRegex.findAll(text).forEach { match ->
        runCatching {
            val url = if (match.value.startsWith("http://") || match.value.startsWith("https://")) {
                match.value
            } else {
                "https://${match.value}"
            }

            val uri = URI(url)
            uri.toURL()

            message = message.replaceText(
                TextReplacementConfig.builder()
                    .match(Regex.escape(match.value))
                    .replacement(
                        buildText {
                            text(match.value)
                            hoverEvent(buildText {
                                info("Klicke hier, um den Link zu öffnen.")
                            })
                            clickOpensUrl(url)
                        }
                    )
                    .build()
            )
        }
    }

    return message
}
