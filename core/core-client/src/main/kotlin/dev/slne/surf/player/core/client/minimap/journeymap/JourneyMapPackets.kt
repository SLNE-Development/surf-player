package dev.slne.surf.player.core.client.minimap.journeymap

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.*

/**
 * Where a player is, as JourneyMap's player radar shows them.
 */
data class JourneyMapPlayer(
    val entityId: Int,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
    val dimension: String,
)

/**
 * Builds the payloads JourneyMap's client reads, in the layout of Minecraft's FriendlyByteBuf.
 */
object JourneyMapPackets {
    // JourneyMap starts most of its payloads with this marker byte.
    private const val MARKER = 42
    private const val MAX_VAR_INT_SIZE = 5

    /**
     * The permissions JourneyMap applies, as the JSON [json]. The companion never claims to be a server admin,
     * to have JourneyMap's server mod or to manage waypoints.
     */
    fun permissions(json: String) = payload {
        writeByte(MARKER)
        writeBoolean(false)
        writeUtf(json)
        writeBoolean(false)
        writeBoolean(false)
    }

    /**
     * Lists [players] for the player radar; a `null` position lists the player as hidden.
     */
    fun playerLocations(players: Map<UUID, JourneyMapPlayer?>) =
        playerLocations(players.map { (uuid, player) -> playerEntry(uuid, player) })

    /**
     * Lists the [entries] [playerEntry] encoded for the player radar.
     */
    fun playerLocations(entries: Collection<ByteArray>): ByteArray {
        val bytes = ByteArrayOutputStream(MAX_VAR_INT_SIZE + entries.sumOf { it.size })
        DataOutputStream(bytes).use { out ->
            out.writeVarInt(entries.size)
            for (entry in entries) {
                out.write(entry)
            }
        }
        return bytes.toByteArray()
    }

    /**
     * One player of [playerLocations]; a `null` position lists the player as hidden.
     */
    fun playerEntry(uuid: UUID, player: JourneyMapPlayer?) = payload {
        writeBoolean(player != null)
        writeUuid(uuid)
        if (player == null) return@payload

        writeVarInt(player.entityId)
        writeDouble(player.x)
        writeDouble(player.y)
        writeDouble(player.z)
        writeByte(angle(player.yaw))
        writeByte(angle(player.pitch))
    }

    fun removePlayer(uuid: UUID) = payload { writeUuid(uuid) }

    // Like JourneyMap's PlayerLoc: 256 steps per full turn.
    private fun angle(degrees: Float) = (degrees * 256f / 360f).toInt().toByte().toInt()

    private fun payload(write: DataOutputStream.() -> Unit): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use(write)
        return bytes.toByteArray()
    }

    private fun DataOutputStream.writeVarInt(value: Int) {
        var remaining = value
        while (remaining and 0x7F.inv() != 0) {
            writeByte(remaining and 0x7F or 0x80)
            remaining = remaining ushr 7
        }
        writeByte(remaining)
    }

    private fun DataOutputStream.writeUtf(value: String) {
        val utf8 = value.toByteArray(Charsets.UTF_8)
        writeVarInt(utf8.size)
        write(utf8)
    }

    private fun DataOutputStream.writeUuid(uuid: UUID) {
        writeLong(uuid.mostSignificantBits)
        writeLong(uuid.leastSignificantBits)
    }
}
