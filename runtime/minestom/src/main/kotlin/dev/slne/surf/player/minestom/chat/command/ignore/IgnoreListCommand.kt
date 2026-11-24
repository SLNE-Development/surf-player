package dev.slne.surf.player.minestom.chat.command.ignore

import dev.slne.surf.api.minestom.command.CommandAPI
import dev.slne.surf.api.minestom.command.CommandTree
import dev.slne.surf.api.minestom.command.dsl.literalArgument
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.messages.pagination.Pagination
import dev.slne.surf.api.core.util.mapAsync
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.util.formatTime
import dev.slne.surf.player.core.common.chat.service.IgnoreService
import dev.slne.surf.core.api.common.SurfCoreApi
import net.kyori.adventure.text.format.TextDecoration
import java.time.OffsetDateTime

private val pagination = Pagination<IgnoreListPaginationEntry> {
    title {
        primary("Ignorierte Spieler".toSmallCaps(), TextDecoration.BOLD)
    }

    rowRenderer { entry, _ ->
        listOf(
            buildText {
                append(CommonComponents.EM_DASH)
                appendSpace()
                variableKey(entry.targetName)
                appendSpace()
                spacer("(${entry.createdAt.formatTime()})")
            }
        )
    }
}

fun CommandTree.ignoreListCommand(): CommandTree = literalArgument("#list") {
    withPermission(ChatPermissions.COMMAND_IGNORE_LIST)
    playerExecutorSuspend { player, _ ->
        val ignoreList = IgnoreService.getCachedIgnoreList(player.uuid)
            .mapAsync {
                IgnoreListPaginationEntry(
                    targetName = SurfCoreApi.getOfflinePlayer(it.target)?.lastKnownName ?: it.target.toString(),
                    createdAt = it.createdAt
                )
            }

        if (ignoreList.isEmpty()) {
            CommandAPI.failWithString("Du ignorierst aktuell niemanden.")
        }

        player.sendText {
            append(pagination.renderComponent(ignoreList))
        }
    }
}

private data class IgnoreListPaginationEntry(
    val targetName: String,
    val createdAt: OffsetDateTime
)
