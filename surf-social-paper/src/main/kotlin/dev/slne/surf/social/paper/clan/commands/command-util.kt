package dev.slne.surf.social.paper.clan.commands

import dev.jorel.commandapi.CommandAPIPaper
import dev.slne.surf.api.core.messages.adventure.buildText

fun failCommand(message: String): Nothing =
    throw CommandAPIPaper.failWithAdventureComponent(buildText {
        appendErrorPrefix()
        error(message)
    })