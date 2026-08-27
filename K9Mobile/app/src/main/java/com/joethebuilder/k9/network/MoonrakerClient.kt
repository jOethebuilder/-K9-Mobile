package com.joethebuilder.k9.network

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Port of u1TestConnection() / u1SendFilamentConfig() from the K-9 .ino.
 * Same endpoints (port 7125, /printer/info, /printer/gcode/script),
 * same SET_PRINT_FILAMENT_CONFIG macro — this is paxx12 Extended Firmware's
 * custom Klipper macro, not stock Anycubic firmware (per Joe's notes: ACE
 * N033 has no network write path of its own).
 */
class MoonrakerClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
) {
    /** Mirrors u1TestConnection(): GET /printer/info, success = HTTP 200. */
    fun testConnection(host: String): Boolean {
        if (host.isBlank()) return false
        return try {
            val url = "http://$host:7125/printer/info"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Mirrors u1SendFilamentConfig(). Known open item carried over from firmware
     * notes: in OpenRFID mode this fails with 003-0522-0000-0000 on slots holding
     * an officially-detected tag; External mode works but disables printer-side
     * tag detection. That's a printer-firmware-mode conflict, not something this
     * client can work around — same tradeoff applies here.
     */
    fun sendFilamentConfig(
        host: String,
        slot: Int,
        vendor: String,
        type: String,
        subtype: String,
        colorHexRgba: String
    ): Boolean {
        if (host.isBlank()) return false

        val gcode = "SET_PRINT_FILAMENT_CONFIG CONFIG_EXTRUDER=$slot" +
            " VENDOR='$vendor'" +
            " FILAMENT_TYPE='$type'" +
            " FILAMENT_SUBTYPE='${subtype.ifBlank { "Basic" }}'" +
            " FILAMENT_COLOR_RGBA=$colorHexRgba"

        return try {
            val url = HttpUrl.Builder()
                .scheme("http")
                .host(host)
                .port(7125)
                .addPathSegments("printer/gcode/script")
                .addQueryParameter("script", gcode)
                .build()
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                response.isSuccessful && body.contains("\"result\"") && body.contains("ok")
            }
        } catch (e: Exception) {
            false
        }
    }
}
