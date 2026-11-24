package dev.slne.surf.player.core.client.chat.command

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.pagination.Pagination
import dev.slne.surf.api.core.util.mapAsync
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.player.api.chat.entry.HistoryFilter
import dev.slne.surf.player.core.client.chat.config.AiModerationConfig
import dev.slne.surf.player.core.client.chat.config.chatConfigProvider
import dev.slne.surf.api.core.service.PlayerLookupService
import dev.slne.surf.player.api.chat.entry.HistoryEntry
import dev.slne.surf.player.core.client.chat.lookup.LookupQuerySuggestions
import dev.slne.surf.player.core.client.chat.lookup.ParsedLookupFilters
import dev.slne.surf.player.core.client.chat.lookup.RenderData
import dev.slne.surf.player.core.client.chat.lookup.lookupPagination
import dev.slne.surf.player.core.client.chat.lookup.parseFilters
import dev.slne.surf.player.core.client.chat.lookup.resolveSenderNames
import dev.slne.surf.player.core.client.chat.message.format.appendStatusIcon
import dev.slne.surf.player.core.client.chat.service.FunctionalityService
import dev.slne.surf.player.core.client.chat.service.HistoryService
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.service.IgnoreRules
import dev.slne.surf.player.core.client.chat.service.IgnoreService
import dev.slne.surf.player.core.client.util.hasNetworkPermission
import dev.slne.surf.player.core.client.chat.service.SlowChatService
import dev.slne.surf.player.core.client.chat.service.SpyService
import dev.slne.surf.player.core.client.chat.util.formatTime
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.command.CommandOutcome
import dev.slne.surf.player.core.client.command.failure
import dev.slne.surf.player.core.client.command.reply
import kotlinx.coroutines.TimeoutCancellationException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import java.time.OffsetDateTime
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * The chat commands every platform runs: the platforms only parse the arguments and hand the
 * outcome back to the sender.
 */
object ChatCommands {
    const val CANNOT_MESSAGE_YOURSELF = "Du kannst dir selbst keine privaten Nachrichten senden."
    const val NO_REPLY_TARGET = "Du hast noch keine privaten Nachrichten erhalten, auf die du antworten könntest."

    // region ignore

    suspend fun ignoreAdd(player: UUID, target: UUID, targetName: String): CommandOutcome {
        IgnoreRules.ignoreRefusal(player, target, targetName, IgnoreService.isIgnored(player, target)) {
            hasNetworkPermission(target, ChatPermissions.IGNORE_EXEMPT)
        }?.let { return failure(it) }

        if (!IgnoreService.ignore(player, target)) {
            return failure("$targetName konnte nicht ignoriert werden. Bitte versuche es erneut.")
        }

        return reply(buildText {
            appendSuccessPrefix()
            success("Du ignorierst nun ")
            variableValue(targetName)
            success(".")
        })
    }

    suspend fun ignoreRemove(player: UUID, target: UUID, targetName: String): CommandOutcome {
        if (!IgnoreService.unignore(player, target)) return failure("Du ignorierst $targetName nicht.")

        return reply(buildText {
            appendSuccessPrefix()
            success("Du ignorierst ")
            variableValue(targetName)
            success(" nicht mehr.")
        })
    }

    /** The names of the players [player] ignores, for suggestions. */
    suspend fun ignoredNames(player: UUID): List<String> =
        IgnoreService.getCachedIgnoreList(player).mapAsync { nameOf(it.target) }

    /** The uuid of the player [player] ignores whose name is [input], ignoring case. */
    suspend fun findIgnored(player: UUID, input: String): UUID? =
        IgnoreService.getCachedIgnoreList(player)
            .mapAsync { it.target to nameOf(it.target) }
            .firstOrNull { (_, name) -> name.equals(input, ignoreCase = true) }
            ?.first

    suspend fun ignoreList(player: UUID, page: Int): CommandOutcome {
        val entries = IgnoreService.getCachedIgnoreList(player).mapAsync {
            IgnoreListEntry(nameOf(it.target), it.createdAt)
        }

        if (entries.isEmpty()) return failure("Du ignorierst aktuell niemanden.")

        return reply(ignoreListPagination.renderComponent(entries, page))
    }

    private data class IgnoreListEntry(val targetName: String, val createdAt: OffsetDateTime)

    private val ignoreListPagination = Pagination<IgnoreListEntry> {
        title { primary("Ignorierte Spieler".toSmallCaps(), TextDecoration.BOLD) }

        rowRenderer { entry, _ ->
            listOf(buildText {
                append(CommonComponents.EM_DASH)
                appendSpace()
                variableKey(entry.targetName)
                appendSpace()
                spacer("(${entry.createdAt.formatTime()})")
            })
        }
    }

    private suspend fun nameOf(uuid: UUID): String =
        SurfCoreApi.getOfflinePlayer(uuid)?.lastKnownName ?: uuid.toString()

    // endregion

    // region spy

    /**
     * [spy] starts reading the private messages of [target], who may be on any server of the
     * network; team members ([ChatPermissions.BYPASS_SPY]) cannot be spied on.
     */
    suspend fun spyAdd(spy: UUID, target: UUID, targetName: String): CommandOutcome {
        if (spy == target) return failure("Du kannst dich nicht selbst ausspionieren.")
        if (hasNetworkPermission(target, ChatPermissions.BYPASS_SPY)) {
            return failure("Du kannst keine Teammitglieder ausspionieren.")
        }
        if (SpyService.isSpying(spy, target)) {
            return failure("Du liest die privaten Nachrichten von $targetName bereits mit.")
        }

        SpyService.addPrivateMessageSpy(spy, target)

        return reply(buildText {
            appendSuccessPrefix()
            success("Du liest nun die privaten Nachrichten von ")
            variableValue(targetName)
            success(" mit.")
        })
    }

    fun spyRemove(spy: UUID, target: UUID, targetName: String): CommandOutcome {
        if (!SpyService.isSpying(spy, target)) {
            return failure("Du liest die privaten Nachrichten von $targetName nicht mit.")
        }

        SpyService.removePrivateMessageSpy(spy, target)

        return reply(buildText {
            appendSuccessPrefix()
            success("Du liest die privaten Nachrichten von ")
            variableValue(targetName)
            success(" nicht mehr mit.")
        })
    }

    fun spyClear(spy: UUID): CommandOutcome {
        if (!SpyService.isSpyingAnyone(spy)) return failure("Du liest bei keinem Spieler mit.")

        SpyService.clearPrivateMessageSpies(spy)

        return reply(buildText {
            appendSuccessPrefix()
            success("Du liest bei keinem Spieler mehr mit.")
        })
    }

    // endregion

    // region slowchat

    private fun stateText(enabled: Boolean) = if (enabled) "aktiviert" else "deaktiviert"

    fun slowChatInfo(): Component = buildText {
        appendInfoPrefix()
        info("Der Slowchat ist derzeit ")
        variableValue(stateText(SlowChatService.isSlowChat()))
        info(".")
    }

    /**
     * Switches the slow chat to [enabled] (or toggles it when `null`); [broadcast] receives the
     * notification for the team.
     */
    fun slowChatToggle(enabled: Boolean?, senderName: String, broadcast: (Component) -> Unit): CommandOutcome {
        val target = enabled ?: !SlowChatService.isSlowChat()

        if (target == SlowChatService.isSlowChat()) {
            return failure("Der Slowchat ist bereits ${stateText(target)}.")
        }

        SlowChatService.setSlowChat(target)

        broadcast(buildText {
            appendInfoPrefix()
            info("Der Slowchat wurde von ")
            variableValue(senderName)
            info(" ")
            variableValue(stateText(target))
            info(".")
        })

        return reply(buildText {
            appendSuccessPrefix()
            success("Der Slowchat wurde ")
            variableValue(stateText(target))
            success(".")
        })
    }

    // endregion

    // region admin

    fun reload(): Component {
        val millis = measureTimeMillis {
            chatConfigProvider.reloadFromFile()
            AiModerationConfig.reload()
        }

        return CommandMessages.configReloaded("chat", millis)
    }

    fun chatStatus(): Component = buildText {
        appendInfoPrefix()
        info("Der Chat ist auf diesem Server ")
        variableValue(stateText(FunctionalityService.getFunctionalities().localChatEnabled))
        info(".")
    }

    /**
     * Switches the chat on this server to [enabled]; [broadcast] receives the notification for the
     * team.
     */
    suspend fun chatToggle(enabled: Boolean, senderName: String, broadcast: (Component) -> Unit): CommandOutcome {
        if (FunctionalityService.getFunctionalities().localChatEnabled == enabled) {
            return failure("Der Chat ist auf diesem Server bereits ${stateText(enabled)}.")
        }

        FunctionalityService.updateLocalFunctionalities { it.copy(localChatEnabled = enabled) }

        broadcast(buildText {
            appendInfoPrefix()
            variableValue(senderName)
            info(" hat den Chat auf dem Server ")
            variableValue(SurfCoreApi.getCurrentServerName())
            info(" ${stateText(enabled)}.")
        })

        return reply(buildText {
            appendSuccessPrefix()
            success("Der Chat wurde auf diesem Server ${stateText(enabled)}.")
        })
    }

    suspend fun chatServers(page: Int): Component {
        val entries = FunctionalityService.getFunctionalitiesForAllServers()
            .map { (server, functionalities) -> server to functionalities.localChatEnabled }

        return serverPagination.renderComponent(entries, page)
    }

    private val serverPagination = Pagination<Pair<String, Boolean>> {
        title { primary("Chat auf den Servern".toSmallCaps(), TextDecoration.BOLD) }
        rowRenderer { (server, enabled), _ ->
            listOf(buildText {
                append(CommonComponents.EM_DASH)
                appendSpace()
                variableKey(server)
                spacer(":")
                appendSpace()
                appendStatusIcon(enabled)
            })
        }
    }

    val lookupSearching: Component = buildText {
        appendInfoPrefix()
        info("Es wird nach Ergebnissen gesucht...")
    }

    /**
     * Looks the chat history up with the filters in [query] (`--sender Name --range 1d ...`).
     */
    suspend fun lookup(query: Map<String, String>?): CommandOutcome =
        lookup(query, { PlayerLookupService.getUuid(it) }, HistoryService::findHistoryEntry)

    /**
     * [lookup] with the player resolution and the history query passed in: an unknown player in a
     * player filter fails with [CommandMessages.PLAYER_NOT_FOUND] before [findHistory] runs.
     */
    internal suspend fun lookup(
        query: Map<String, String>?,
        resolvePlayer: suspend (String) -> UUID?,
        findHistory: suspend (HistoryFilter) -> List<HistoryEntry>,
    ): CommandOutcome {
        val filter = when (val parsed = query?.parseFilters(resolvePlayer)) {
            null -> HistoryFilter.empty()
            is ParsedLookupFilters.UnknownPlayer -> return failure(CommandMessages.PLAYER_NOT_FOUND)
            is ParsedLookupFilters.Valid -> parsed.filter
        }
        val page = query?.get("--page")?.toIntOrNull() ?: 1

        val history = try {
            findHistory(filter)
        } catch (_: TimeoutCancellationException) {
            return failure("Die Suche hat zu lange gedauert und wurde abgebrochen.")
        }

        if (history.isEmpty()) return failure("Es wurden keine Ergebnisse gefunden.")

        val senderNames = resolveSenderNames(history)
        val renderData = history.map { RenderData(senderNames, it) }

        return reply(lookupPagination.renderComponent(renderData, page))
    }

    private val queryPairRegex = Regex("""\s+(?=--)""")

    /**
     * Splits a lookup query typed as one string (`--sender Name --message "Hallo Welt"`) into its
     * filters, for platforms without a map argument.
     */
    fun parseLookupQuery(query: String): Map<String, String> = query.trim()
        .split(queryPairRegex)
        .filter { it.startsWith("--") }
        .associate { pair -> pair.substringBefore(' ') to pair.substringAfter(' ', "").trim() }

    /** The filters `/surfplayer chat lookup` understands. */
    val lookupFilterKeys = LookupQuerySuggestions.filterKeys

    /**
     * The tab completions of the lookup query typed so far ([input]): filter names, and after a
     * filter name its values; player names come from [playerNames] (the platform's visible
     * players), server names from surf-core.
     */
    fun lookupSuggestions(input: String, playerNames: (prefix: String) -> Collection<String>): List<String> =
        LookupQuerySuggestions.suggest(input, playerNames) {
            SurfCoreApi.getServers().map { it.name }.sorted()
        }

    private data class FilterHelp(val key: String, val description: String, val example: String)

    private val filterHelp = listOf(
        FilterHelp("type", "Nachrichtentyp, z. B. TEAM, GLOBAL, DIRECT", "--type team"),
        FilterHelp("range", "Zeitraum bis jetzt: 10s, 10m, 5h, 1d, 7w", "--range 1d"),
        FilterHelp("message", "Text, den die Nachricht enthält (ohne Groß-/Kleinschreibung)", "--message \"Hallo Welt\""),
        FilterHelp("sender", "Absender der Nachricht", "--sender TheBjoRedCraft"),
        FilterHelp("receiver", "Empfänger der Nachricht", "--receiver Keviro"),
        FilterHelp("server", "Server, auf dem die Nachricht geschrieben wurde", "--server survival01"),
        FilterHelp("deleted", "Nur gelöschte (true) oder nicht gelöschte (false) Nachrichten", "--deleted true"),
        FilterHelp("deletedBy", "Spieler, der die Nachricht gelöscht hat", "--deletedBy Jo_field"),
        FilterHelp("messageUuid", "UUID der Nachricht", "--messageUuid 1c779cb1-3860-4e23-9cac-7f160b2acc61"),
        FilterHelp("limit", "Höchstzahl der Ergebnisse, neueste zuerst (Standard: 50)", "--limit 10"),
        FilterHelp("page", "Seite der Ergebnisse", "--page 2"),
    )

    private val filterPagination = Pagination<FilterHelp> {
        title { primary("Lookup-Filter".toSmallCaps(), TextDecoration.BOLD) }
        rowRenderer { help, _ ->
            listOf(buildText {
                append(CommonComponents.EM_DASH)
                appendSpace()
                variableKey("--${help.key}")
                spacer(":")
                appendSpace()
                variableValue(help.description)
                appendNewline()
                repeat(5) { appendSpace() }
                spacer("z. B. ${help.example}")
            })
        }
    }

    fun lookupFilters(page: Int): Component = filterPagination.renderComponent(filterHelp, page)

    // endregion
}
