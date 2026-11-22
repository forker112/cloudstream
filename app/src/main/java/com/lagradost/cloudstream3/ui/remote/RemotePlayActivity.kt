package com.lagradost.cloudstream3.ui.remote

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.network.RemotePlayServer
import com.lagradost.cloudstream3.utils.getLocalIpAddress

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
            // ZXing দিয়ে QR কোড তৈরি
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, 400, 400)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            ivQrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            tvStatus.text = "Failed to generate QR Code: ${e.message}"
        }

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
}
