package com.example.misi_budaya.util

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.delay

/**
 * Small util to verify actual internet reachability (not just network capability).
 * Uses Google generate_204 endpoint which returns 204 quickly when internet is available.
 */
object NetworkUtil {
    fun hasInternetConnection(timeoutMs: Int = 1500): Boolean {
        return try {
            val url = URL("https://www.gstatic.com/generate_204")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                requestMethod = "GET"
            }
            conn.connect()
            val code = conn.responseCode
            conn.disconnect()
            code == 204
        } catch (e: Exception) {
            false
        }
    }

    suspend fun waitForInternet(timeoutMs: Long = 10_000L, pollIntervalMs: Long = 500L): Boolean {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (hasInternetConnection()) return true
            delay(pollIntervalMs)
        }
        return false
    }
}
