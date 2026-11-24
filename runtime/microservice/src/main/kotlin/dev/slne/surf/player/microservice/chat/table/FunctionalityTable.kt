package dev.slne.surf.player.microservice.chat.table

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.ULongIdTable
import dev.slne.surf.player.microservice.PlayerSchemas

object FunctionalityTable : ULongIdTable("${PlayerSchemas.chat}.chat_functionalities") {
    val server = varchar("server", 256).uniqueIndex()
    val chatEnabled = bool("chat_enabled").default(true)
}