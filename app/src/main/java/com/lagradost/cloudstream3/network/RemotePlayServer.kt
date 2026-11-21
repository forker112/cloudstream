package com.lagradost.cloudstream3.network

import android.content.Context
import android.content.Intent
import com.lagradost.cloudstream3.MainActivity
import fi.iki.elonen.NanoHTTPD

class RemotePlayServer(
    private val context: Context,
    port: Int = 8080
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        // শুধুমাত্র /play লিংকে রিকোয়েস্ট এলেই কাজ করবে
        if (session.uri != "/play") {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found")
        }

        // ফোন থেকে পাঠানো url প্যারামিটারটি খুঁজবে
        val videoUrl = session.parameters["url"]?.firstOrNull()
        if (videoUrl.isNullOrBlank()) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Missing url")
        }

        // টিভির MainActivity-তে ভিডিও লিংকটি পাঠানোর জন্য ইন্টেন্ট
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("play_url", videoUrl)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)

        // ফোনকে সফল মেসেজ পাঠানো
        return newFixedLengthResponse(Response.Status.OK, "text/plain", "OK")
    }
}
