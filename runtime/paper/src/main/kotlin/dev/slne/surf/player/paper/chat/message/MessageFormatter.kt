@file:Suppress("RETURN_IN_FUNCTION_WITH_EXPRESSION_BODY_WARNING")

package dev.slne.surf.player.paper.chat.message

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.plain
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.core.client.chat.config.chatConfig
import dev.slne.surf.player.api.settings.SurfSettingsApi
import dev.slne.surf.player.api.settings.setting.SettingKeys
import dev.slne.surf.player.core.client.chat.message.format.appendDelete
import dev.slne.surf.player.core.client.chat.util.formatTime
import dev.slne.surf.player.core.client.chat.util.updateLinks
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.paper.chat.util.appendName
import dev.slne.surf.player.paper.chat.util.playPingSound
import dev.slne.surf.player.core.client.chat.message.format.MentionRules
import dev.slne.surf.player.core.client.vanish.PlayerVisibility
import dev.slne.surf.player.paper.command.asViewer
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Pattern

/**
 * Formats messages that require access to Paper specific player state.
 */
object MessageFormatter {
    private val itemRegex = Regex("\\[(?i)item]")

    private val mentionCacheLock = Any()
    private val mentionGeneration = AtomicLong()

    @Volatile
    private var mentionCache: MentionCache? = null

    /**
     * Marks the mention cache stale; the next render rebuilds it.
     */
    fun invalidateMentionCache() {
        mentionGeneration.incrementAndGet()
    }

    /**
     * Whether [plainMessage] mentions any online player.
     */
    fun hasMention(plainMessage: String): Boolean {
        val pattern = mentionCache().pattern ?: return false
        return pattern.matcher(plainMessage).find()
    }

    fun formatGlobal(messageData: MessageData, viewer: UUID, highlightMentions: Boolean) = buildText {
        val senderPlayer = server.getPlayer(messageData.sender) ?: return Component.empty()
        val viewerPlayer = server.getPlayer(viewer)

        if (viewerPlayer != null && viewerPlayer.hasPermission(ChatPermissions.DELETE_MESSAGE)) {
            appendDelete(messageData)
        }

        appendName(
            senderPlayer,
            showTeleportButton = viewerPlayer != null && viewerPlayer.hasPermission(ChatPermissions.TELEPORT)
        )
        darkSpacer(":")
        appendSpace()

        var content = messageData.message
        if (highlightMentions && viewerPlayer != null) {
            content = highlightPlayers(content, viewer, viewerPlayer, senderPlayer)
        }

        append {
            append(formatItemTag(updateLinks(content), senderPlayer, viewer, messageData.plainMessage))
            hoverEvent(buildText {
                spacer("(${messageData.sentAt.formatTime()})")
                appendSpace()
                white(messageData.plainMessage)
            })
        }
        clickSuggestsCommand("/msg ${senderPlayer.name} ")
    }

    private fun formatItemTag(
        rawMessage: Component,
        player: Player,
        viewer: UUID?,
        sourcePlain: String
    ): Component {
        if (!chatConfig.itemPlaceholder) {
            return rawMessage
        }

        if (!itemRegex.containsMatchIn(sourcePlain)) {
            return rawMessage
        }

        var message = rawMessage

        if (!itemRegex.containsMatchIn(message.plain())) {
            return message
        }

        val item = player.inventory.itemInMainHand

        if (item.type == Material.AIR) {
            if (viewer == null || player.uniqueId == viewer) {
                player.sendText {
                    appendErrorPrefix()
                    error("Du hast kein Item in der Hand!")
                }
            }

            return message
        }

        message = message.replaceText(
            TextReplacementConfig
                .builder()
                .match(itemRegex.pattern)
                .replacement(buildText {
                    if (item.amount > 1) {
                        variableValue("${item.amount}x ")
                    }
                    append(item.displayName())
                })
                .build()
        )

        return message
    }

    private fun mentionCache(): MentionCache {
        val generation = mentionGeneration.get()
        val cached = mentionCache

        if (cached != null && cached.generation == generation) {
            return cached
        }

        return rebuildMentionCache()
    }

    private fun rebuildMentionCache(): MentionCache = synchronized(mentionCacheLock) {
        val generation = mentionGeneration.get()
        val cached = mentionCache

        if (cached != null && cached.generation == generation) {
            return cached
        }

        val players = server.onlinePlayers
        val playersByName = Object2ObjectOpenHashMap<String, Player>(players.size * 2)
        val patternBuilder = StringBuilder(players.size * 24)

        for (player in players) {
            val name = player.name
            playersByName[name.lowercase()] = player

            if (patternBuilder.isNotEmpty()) {
                patternBuilder.append('|')
            }
            patternBuilder.append(Pattern.quote(name))
        }

        val rebuilt = MentionCache(
            generation,
            if (players.isEmpty()) null else Pattern.compile(
                "@?($patternBuilder)\\b",
                Pattern.CASE_INSENSITIVE
            ),
            playersByName
        )

        mentionCache = rebuilt
        rebuilt
    }

    /**
     * Highlights the mentions in [rawMessage] that [viewer] may see and pings [viewer] if they are
     * mentioned by a [sender] who may see them, see [MentionRules].
     */
    private fun highlightPlayers(
        rawMessage: Component,
        viewerUuid: UUID,
        viewer: Player,
        sender: Player
    ): Component {
        val cache = mentionCache()
        val pattern = cache.pattern ?: return rawMessage
        val players = cache.playersByName

        val viewerSight = viewer.asViewer()
        val senderSight = sender.asViewer()
        var pingViewer = false

        val message = rawMessage.replaceText(
            TextReplacementConfig.builder()
                .match(pattern)
                .replacement { match ->
                    val fullMatch = match.build().plain()
                    val name = fullMatch.removePrefix("@").lowercase()

                    val player = players[name] ?: return@replacement match

                    val decision = MentionRules.decide(
                        PlayerVisibility.network, senderSight, viewerSight, player.uniqueId
                    )
                    if (!decision.highlight) {
                        return@replacement match
                    }

                    if (decision.ping) {
                        pingViewer = true
                    }

                    buildText {
                        text("@")
                        text(player.name)
                        color(Colors.VARIABLE_VALUE)
                        decorate(TextDecoration.BOLD)
                        clickSuggestsCommand("/msg ${player.name} ")
                    }
                }
                .build()
        )

        if (pingViewer && SurfSettingsApi.getSettingValue(viewerUuid, SettingKeys.CHAT_PINGS)) {
            viewer.playPingSound()
        }

        return message
    }
}
