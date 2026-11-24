package dev.slne.surf.social.api.shrieker

import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.social.api.shrieker.type.ReportType
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key

typealias ReportData = Map<Key, String>

@Serializable
data class Report(
    val reporter: SerializableUUID,
    val reported: SerializableUUID,
    val reportedAt: SerializableOffsetDateTime,

    val type: ReportType,
    val reportData: ReportData,

    val resolved: Boolean,
    val resolvedResult: Boolean?,
    val resolvedBy: SerializableUUID?,
    val resolvedAt: SerializableOffsetDateTime?,

    val internalId: Long,
)