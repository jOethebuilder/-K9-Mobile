package com.joethebuilder.k9.network

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

data class SpoolInfo(
    val id: Int,
    val name: String,
    val material: String,
    val color: String,
    val remainingWeight: Double
)

class SpoolmanClient(private val baseUrl: String = "http://192.168.1.37:7912") {

    private val client = OkHttpClient()

    // GET /api/v1/spool - list all spools
    fun listSpools(): List<SpoolInfo> {
        val request = Request.Builder()
            .url("$baseUrl/api/v1/spool")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val body = response.body?.string() ?: return emptyList()
            val arr = JSONArray(body)
            val spools = mutableListOf<SpoolInfo>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                                val filament = obj.optJSONObject("filament")
                val vendor = filament?.optJSONObject("vendor")
                val filamentName = filament?.optString("name", "") ?: ""
                val vendorName = vendor?.optString("name", "") ?: ""
                val displayName = listOf(vendorName, filamentName).filter { it.isNotBlank() }.joinToString(" ")
                spools.add(
                    SpoolInfo(
                        id = obj.optInt("id"),
                        name = displayName.ifBlank { "Spool ${obj.optInt("id")}" },
                        material = filament?.optString("material", "?") ?: "?",
                        color = filament?.optString("color_hex", "") ?: "",
                        remainingWeight = obj.optDouble("remaining_weight", 0.0)
                    )
                )
            }
            return spools
        }
    }
}
