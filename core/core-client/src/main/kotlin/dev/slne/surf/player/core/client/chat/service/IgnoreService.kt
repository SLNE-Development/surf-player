package dev.slne.surf.player.core.client.chat.service

import com.github.benmanes.caffeine.cache.Caffeine
import dev.slne.surf.player.api.chat.entry.IgnoreListEntry
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import dev.slne.surf.player.core.client.chat.rpc.chatIgnoreRpcService
import dev.slne.surf.player.core.client.friends.FriendsClient
import java.time.OffsetDateTime
import java.util.*

/**
 * The ignore lists of the players on this server.
 */
object IgnoreService {
    private val cache = Caffeine.newBuilder()
        .maximumSize(10_000)
        .build<UUID, List<IgnoreListEntry>>()

    suspend fun ignore(uuid: UUID, ignoredUUID: UUID): Boolean {
        val ignored = chatIgnoreRpcService.createIgnore(uuid, ignoredUUID)
        if (ignored) {
            cache.asMap().compute(uuid) { _, ignoreList ->
                if (ignoreList == null) {
                    listOf(IgnoreListEntry(uuid, ignoredUUID, OffsetDateTime.now()))
                } else {
                    ignoreList + IgnoreListEntry(uuid, ignoredUUID, OffsetDateTime.now())
                }
            }

            // The microservice already deleted these requests while storing the ignore.
            FriendsClient.removeFriendRequestsBetween(uuid, ignoredUUID)
        }

        return ignored
    }

    suspend fun unignore(uuid: UUID, ignoredUUID: UUID): Boolean {
        val unignored = chatIgnoreRpcService.deleteIgnore(uuid, ignoredUUID)
        if (unignored) {
            cache.asMap().compute(uuid) { _, ignoreList ->
                ignoreList?.filterNot { it.target == ignoredUUID }
            }
        }

        return unignored
    }

    fun isIgnored(uuid: UUID, ignoredUUID: UUID): Boolean {
        val entries = cache.getIfPresent(uuid) ?: return false

        for (index in entries.indices) {
            if (entries[index].target == ignoredUUID) {
                return true
            }
        }

        return false
    }

    /**
     * Whether a message from [sender], who must be on this server, is kept from [receiver]
     * because [receiver] ignores them, see [IgnoreRules.blocksMessage].
     */
    fun blocksMessage(receiver: UUID, sender: UUID): Boolean =
        IgnoreRules.blocksMessage(isIgnored(receiver, sender)) {
            ChatPlatform.hasPermission(sender, ChatPermissions.IGNORE_EXEMPT)
        }

    fun getCachedIgnoreList(uuid: UUID): List<IgnoreListEntry> {
        return cache.getIfPresent(uuid) ?: emptyList()
    }

    suspend fun loadIgnoreList(uuid: UUID): List<IgnoreListEntry> {
        val ignoreList = chatIgnoreRpcService.findIgnoreList(uuid)
        cache.put(uuid, ignoreList)
        return ignoreList
    }

    fun cleanup(uuid: UUID) {
        cache.invalidate(uuid)
    }
}
