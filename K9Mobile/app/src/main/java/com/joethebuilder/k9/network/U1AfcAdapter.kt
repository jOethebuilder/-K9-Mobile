package com.joethebuilder.k9.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class U1AfcAdapter(
    private val moonrakerBaseUrl: String = "http://192.168.1.19:7125"
) : SlotSpoolAdapter {

    override val slotCount = 4
    private val lanes = listOf("E0", "E1", "E2", "E3")
    private val client = OkHttpClient()
    private val jsonMedia = "application/json".toMediaType()

    override fun readSlots(): List<SlotAssignment> {
        val objects = lanes.joinToString("&") { "AFC_lane%20$it" }
        val request = Request.Builder()
            .url("$moonrakerBaseUrl/printer/objects/query?$objects")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val status = JSONObject(body).optJSONObject("result")?.optJSONObject("status")
                ?: return emptyList()

            return lanes.mapIndexed { index, lane ->
                val laneObj = status.optJSONObject("AFC_lane $lane")
                val spoolId = laneObj?.optInt("spool_id", -1)?.takeIf { it > 0 }
                SlotAssignment(slot = index, spoolId = spoolId)
            }
        }
    }

    override fun assignSpool(slot: Int, spoolId: Int): Boolean {
        val lane = lanes.getOrNull(slot) ?: return false
        val script = "SET_SPOOL_ID LANE=$lane SPOOL_ID=$spoolId"
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
