package com.lagradost.cloudstream3.ui.remote

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.network.RemotePlayServer
import com.lagradost.cloudstream3.utils.getLocalIpAddress
import io.github.g0dkar.qrcode.QRCode
import io.github.g0dkar.qrcode.render.QRCodeGraphicsFactory
import java.awt.image.BufferedImage

class RemotePlayActivity : AppCompatActivity() {

    private var server: RemotePlayServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_play)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val ivQrCode = findViewById<ImageView>(R.id.ivQrCode)

        val ip = getLocalIpAddress()
        if (ip == null) {
            tvStatus.text = "No Wi-Fi connection found!"
            tvStatus.setTextColor(android.graphics.Color.RED)
            return
        }

        val url = "http://$ip:8080/play"
        tvStatus.text = "Connect to: $url"

        try {
            // কিউআর কোড তৈরি
            val qrCode = QRCode.ofSquares().build(url)
            val bufferedImage = qrCode.render(QRCodeGraphicsFactory.newFactory())
            val bitmap = bufferedImage.toBitmap()
            ivQrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            tvStatus.text = "Failed to generate QR Code"
        }

        // সার্ভার চালু করুন
        try {
            server = RemotePlayServer(this)
            server?.start()
        } catch (e: Exception) {
            tvStatus.text = "Failed to start server: ${e.message}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop()
    }

    // BufferedImage থেকে Android Bitmap-এ কনভার্ট করার হেল্পার ফাংশন
    private fun BufferedImage.toBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        getRGB(0, 0, width, height, pixels, 0, width)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}
