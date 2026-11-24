package dev.slne.surf.player.core.client.friends.util

import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.text.Component
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

fun OffsetDateTime.format(): String = format(DATE_TIME_FORMATTER)
fun OffsetDateTime.formatComponent() = buildText {
    variableValue(format())
}

/** Formats a remaining wait time in whole minutes, e.g. "23 Minuten" or "1 Tag : 2 Stunden". */
fun Duration.formatRemainingComponent(): Component =
    CommonComponents.formatTime(roundUpToMinutes(), showSeconds = false, shortForms = false)

/**
 * Rounds up to whole minutes, but to at least one minute, so a wait that is not over yet never
 * reads as nothing.
 */
fun Duration.roundUpToMinutes(): Duration =
    ceil(toDouble(DurationUnit.MINUTES)).toLong().coerceAtLeast(1).minutes
