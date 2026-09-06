package com.joethebuilder.k9.network

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * Sends filament settings to a Bambu Lab printer, mirroring what
 * MoonrakerClient does for the Snapmaker U1 — same job (tap a tag, push
 * its settings to the printer), different transport, since Bambu printers
 * don't run Moonraker: they run their own local MQTT broker.
 *
 * Connection: MQTT over TLS, port 8883, username "bblp", password = the
 * printer's 8-digit LAN access code (Settings > WLAN > Access Code on the
 * printer). Requires LAN Mode + Developer Mode enabled on the printer —
 * NOT LAN-Only Mode. Command reference: Doridian/OpenBambuAPI mqtt.md,
 * cross-checked against spuder/OpenSpool's own Bambu MQTT config.
 *
 * TLS note: the printer uses a self-signed certificate for its local
 * broker. This client trusts it without verifying the chain (same
 * approach OpenSpool's own config takes with `skip_cert_cn_check: true`)
 * since this only ever talks to a printer on the same local network, not
 * over the internet.
 *
 * Targets the EXTERNAL SPOOL slot (ams_id 255, tray_id 254) — the slot
 * relevant for a tap-a-tag workflow, not a specific AMS unit's tray.
 */
class BambuMqttClient {

    fun sendFilamentConfig(
        host: String,
        serial: String,
        accessCode: String,
        materialType: String,
        colorHex: String,
        nozzleTempMin: Int,
        nozzleTempMax: Int
    ): Boolean {
        if (host.isBlank() || serial.isBlank() || accessCode.isBlank()) return false

        val client = MqttClient("ssl://$host:8883", "k9mobile-${System.currentTimeMillis()}", MemoryPersistence())
        return try {
            val options = MqttConnectOptions().apply {
                userName = "bblp"
                password = accessCode.toCharArray()
                isCleanSession = true
                connectionTimeout = 5
                socketFactory = trustingSslSocketFactory()
            }
            client.connect(options)

            val payload = JSONObject().apply {
                put("print", JSONObject().apply {
                    put("sequence_id", "0")
                    put("command", "ams_filament_setting")
                    put("ams_id", 255)
                    put("tray_id", 254)
                    put("tray_type", materialType)
                    // Bambu wants 8-char RGBA hex (color + alpha); force full
                    // opacity if a 6-char RGB hex was passed in instead.
                    put("tray_color", if (colorHex.length == 6) "${colorHex}FF".uppercase() else colorHex.uppercase())
                    put("nozzle_temp_min", nozzleTempMin)
                    put("nozzle_temp_max", nozzleTempMax)
                    put("setting_id", "")
                })
            }

            val message = MqttMessage(payload.toString().toByteArray(Charsets.UTF_8))
            message.qos = 1
            client.publish("device/$serial/request", message)
            client.disconnect()
            true
        } catch (e: Exception) {
            try { client.disconnect() } catch (_: Exception) {}
            false
        }
    }

    fun testConnection(host: String, serial: String, accessCode: String): Boolean {
        if (host.isBlank() || serial.isBlank() || accessCode.isBlank()) return false
        val client = MqttClient("ssl://$host:8883", "k9mobile-test-${System.currentTimeMillis()}", MemoryPersistence())
        return try {
            val options = MqttConnectOptions().apply {
                userName = "bblp"
                password = accessCode.toCharArray()
                isCleanSession = true
                connectionTimeout = 5
                socketFactory = trustingSslSocketFactory()
            }
            client.connect(options)
            client.disconnect()
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Same-LAN-only trust model — see class doc. Not for use over the open internet. */
    private fun trustingSslSocketFactory(): javax.net.ssl.SSLSocketFactory {
        val trustAll = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        val context = SSLContext.getInstance("TLS")
        context.init(null, arrayOf(trustAll), SecureRandom())
        return context.socketFactory
    }
}
