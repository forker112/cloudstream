package com.lagradost.cloudstream3.utils

import java.net.Inet4Address
import java.net.NetworkInterface

fun getLocalIpAddress(): String? {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (intf in interfaces) {
            if (!intf.isUp || intf.isLoopback) continue
            for (addr in intf.inetAddresses) {
                if (addr is Inet4Address && !addr.isLoopbackAddress) {
                    return addr.hostAddress
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}
