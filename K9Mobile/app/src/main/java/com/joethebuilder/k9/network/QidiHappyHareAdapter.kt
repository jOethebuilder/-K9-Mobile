package com.joethebuilder.k9.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class QidiHappyHareAdapter(
    private val moonrakerBaseUrl: String
) : SlotSpoolAdapter {

    override val slotCount = 4
    private val client = OkHttpClient()
    private val jsonMedia = "application/json".toMediaType()

    override fun readSlots(): List<SlotAssignment> {
        val request = Request.Builder()
            .url("$moonrakerBaseUrl/printer/objects/query?mmu")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val mmu = JSONObject(body).optJSONObject("result")
                ?.optJSONObject("status")?.optJSONObject("mmu")
                ?: return emptyList()

            val gateSpoolIds = mmu.optJSONArray("gate_spool_id") ?: return emptyList()
            return (0 until gateSpoolIds.length()).map { i ->
                val id = gateSpoolIds.optInt(i, -1)
                SlotAssignment(slot = i, spoolId = id.takeIf { it > 0 })
            }
        }
    }

    override fun assignSpool(slot: Int, spoolId: Int): Boolean {
        val script = "MMU_GATE_MAP GATE=$slot SPOOLID=$spoolId"
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
