package com.joethebuilder.k9.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * QIDI Box + Spoolman integration (jOethebuilder/qidi-box-spoolman-plus4).
 * Reads/writes Klipper save_variables directly.
 * Slot numbering: 0-3 = Box 1 slots 1-4, 4-7 = Box 2 slots 1-4.
 */
class QidiBoxSpoolmanAdapter(
    private val moonrakerBaseUrl: String
) : SlotSpoolAdapter {
    override val slotCount = 8
    private val client = OkHttpClient()
    private val jsonMedia = "application/json".toMediaType()

    private fun keyFor(slot: Int): String {
        val box = (slot / 4) + 1
        val slotInBox = (slot % 4) + 1
        return "box${box}_slot${slotInBox}__spool_id"
    }

    override fun readSlots(): List<SlotAssignment> {
        val request = Request.Builder()
            .url("$moonrakerBaseUrl/printer/objects/query?save_variables=variables")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val variables = JSONObject(body).optJSONObject("result")
                ?.optJSONObject("status")?.optJSONObject("save_variables")
                ?.optJSONObject("variables") ?: return emptyList()
            return (0 until slotCount).map { slot ->
                val id = if (variables.isNull(keyFor(slot))) -1 else variables.optInt(keyFor(slot), -1)
                SlotAssignment(slot = slot, spoolId = id.takeIf { it > 0 })
            }
        }
    }

    override fun assignSpool(slot: Int, spoolId: Int): Boolean {
        val script = "SAVE_VARIABLE VARIABLE=${keyFor(slot)} VALUE=$spoolId"
        val json = JSONObject().put("script", script).toString()
        val request = Request.Builder()
            .url("$moonrakerBaseUrl/printer/gcode/script")
            .post(json.toRequestBody(jsonMedia))
            .build()
        client.newCall(request).execute().use { response ->
            return response.isSuccessful
        }
    }
}
