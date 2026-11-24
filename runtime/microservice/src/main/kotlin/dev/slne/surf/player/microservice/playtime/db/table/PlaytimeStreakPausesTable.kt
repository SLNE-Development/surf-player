package dev.slne.surf.player.microservice.playtime.db.table

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.javatime.date
import dev.slne.surf.player.microservice.PlayerSchemas

object PlaytimeStreakPausesTable : LongIdTable("${PlayerSchemas.playtime}.playtime_streak_pauses") {
    val startDate = date("start_date")
    val endDate = date("end_date")
}
