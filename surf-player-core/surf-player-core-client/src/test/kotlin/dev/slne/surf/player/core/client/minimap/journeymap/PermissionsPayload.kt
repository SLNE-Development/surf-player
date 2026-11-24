package dev.slne.surf.player.core.client.minimap.journeymap

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Reads the JSON back out of a permissions payload: marker, admin flag, then the length-prefixed JSON.
 */
fun permissionsJson(payload: ByteArray): JsonObject {
    var index = 2
    var length = 0
    var shift = 0
    while (true) {
        val byte = payload[index++].toInt()
        length = length or (byte and 0x7F shl shift)
        if (byte and 0x80 == 0) break
        shift += 7
    }
    return Json.parseToJsonElement(payload.decodeToString(index, index + length)).jsonObject
}
