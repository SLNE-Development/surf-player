package dev.slne.surf.player.core.client.clan.chat

import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.settings.setting.SettingKeys
import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import java.util.*

/**
 * Sends the clan chat message [signedMessage] of [senderUuid], shown as [formattedMessage], to
 * every member of [clan] on any server.
 *
 * Members who turned off [SettingKeys.CLAN_CHAT_MESSAGES] do not see it; that is checked by the
 * server hosting each member. The sender always sees their own message.
 */
suspend fun sendClanChatMessage(
    senderUuid: UUID,
    clan: Clan,
    signedMessage: SignedMessage,
    formattedMessage: Component
) = coroutineScope {
    for (member in clan.members) {
        val requiredSetting = SettingKeys.CLAN_CHAT_MESSAGES.name.takeIf { member.uuid != senderUuid }

        launch {
            ChatPlatform.sendSignedMessage(
                senderUuid,
                member.uuid,
                signedMessage,
                formattedMessage,
                requiredSetting
            )
        }
    }
}
