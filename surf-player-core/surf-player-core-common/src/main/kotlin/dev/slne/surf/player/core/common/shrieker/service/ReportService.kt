package dev.slne.surf.social.core.common.shrieker.service

import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.rpc.RpcService
import dev.slne.surf.social.api.shrieker.Report
import dev.slne.surf.social.api.shrieker.ReportData
import dev.slne.surf.social.api.shrieker.type.ReportType
import java.util.*

@RpcService
interface ReportService {
    suspend fun report(
        reporter: SerializableUUID,
        reported: SerializableUUID,
        type: ReportType,
        data: ReportData,
        reportedAt: SerializableOffsetDateTime
    ): Report

    suspend fun findReportsByReporter(reporter: UUID)
    suspend fun findReportsByReported(reported: UUID)
}