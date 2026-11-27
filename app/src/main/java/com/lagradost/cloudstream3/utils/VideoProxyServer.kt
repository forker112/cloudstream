package com.lagradost.cloudstream3.utils

import fi.iki.elonen.NanoHTTPD
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class VideoProxyServer(
    private val originalUrl: String,
    private val headers: Map<String, String>?,
    port: Int = 8081
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession?): Response {
        session ?: return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Bad Request")

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val requestBuilder = Request.Builder().url(originalUrl)

        headers?.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }

        val rangeHeader = session.headers["range"]
        if (rangeHeader != null) {
            requestBuilder.header("Range", rangeHeader)
        }

        return try {
            val response = client.newCall(requestBuilder.build()).execute()
            val responseBody = response.body ?: return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "No body")

            val inputStream = responseBody.byteStream()
            val status = if (response.code == 206) Response.Status.PARTIAL_CONTENT else Response.Status.OK
            val mimeType = response.header("Content-Type", "video/mp4")
            val contentLength = responseBody.contentLength()

            val nanoResponse = if (contentLength > 0) {
                newFixedLengthResponse(status, mimeType, inputStream, contentLength)
            } else {
                newChunkedResponse(status, mimeType, inputStream)
            }

            response.headers.forEach { (name, value) ->
                nanoResponse.addHeader(name, value)
            }
            nanoResponse
        } catch (e: Exception) {
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Proxy Error: ${e.message}")
        }
    }
}
