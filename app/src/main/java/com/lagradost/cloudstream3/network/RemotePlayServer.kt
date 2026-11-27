package com.lagradost.cloudstream3.network

import android.content.Context
import android.content.Intent
import com.lagradost.cloudstream3.MainActivity
import com.lagradost.cloudstream3.utils.VideoProxyServer
import com.lagradost.cloudstream3.utils.getLocalIpAddress
import fi.iki.elonen.NanoHTTPD

class RemotePlayServer(
    private val context: Context,
    port: Int = 8080
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        if (session.uri != "/play") {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found")
        }

        val videoUrl = session.parameters["url"]?.firstOrNull()
        if (videoUrl.isNullOrBlank()) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Missing url")
        }

        // ফোন থেকে পাঠানো হেডারগুলো রিসিভ করা
        val headersString = session.parameters["headers"]?.firstOrNull()
        val headerMap = mutableMapOf<String, String>()
        if (!headersString.isNullOrBlank()) {
            headersString.split("|").forEach {
                val parts = it.split(":", limit = 2)
                if (parts.size == 2) {
                    headerMap[parts[0]] = parts[1]
                }
            }
        }

        // প্রক্সি সার্ভার চালু করা (পোর্ট 8081)
        val proxyServer = VideoProxyServer(videoUrl, headerMap, 8081)
        try {
            proxyServer.start()
        } catch (e: Exception) {
            // ইতিমধ্যে চালু থাকলে সমস্যা নেই
        }

        // প্লেয়ারকে প্রক্সি সার্ভারের লোকাল লিংকটি পাঠানো
        val ip = getLocalIpAddress() ?: "127.0.0.1"
        val proxyUrl = "http://$ip:8081/proxy"

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("play_url", proxyUrl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)

        return newFixedLengthResponse(Response.Status.OK, "text/plain", "OK")
    }
}
