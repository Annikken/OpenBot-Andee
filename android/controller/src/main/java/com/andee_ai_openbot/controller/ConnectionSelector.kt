package com.andee_ai_openbot.controller

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager

@SuppressLint("StaticFieldLeak")
object ConnectionSelector {

    private val connection: com.andee_ai_openbot.controller.ILocalConnection? = null
    private const val TAG = "ConnectionManager"
    @SuppressLint("StaticFieldLeak")
    private lateinit var context: Context

    fun init (context: Context) {
        com.andee_ai_openbot.controller.ConnectionSelector.context = context
    }

    fun getConnection(): com.andee_ai_openbot.controller.ILocalConnection {
        val connected = com.andee_ai_openbot.controller.ConnectionSelector.isConnectedViaWifi(com.andee_ai_openbot.controller.ConnectionSelector.context) || com.andee_ai_openbot.controller.ConnectionSelector.isWifiApEnabled(com.andee_ai_openbot.controller.ConnectionSelector.context)
        return if (connected) com.andee_ai_openbot.controller.NetworkServiceConnection else com.andee_ai_openbot.controller.NearbyConnection
    }

    private fun isConnectedViaWifi(context: Context): Boolean {

        val wifiManager = context.getSystemService(WIFI_SERVICE) as WifiManager?
        val info = wifiManager!!.connectionInfo ?: return false
        val networkId = info.networkId

        return networkId > 0
    }

    private fun isWifiApEnabled(context: Context): Boolean {
        val wifiManager = context.getSystemService(WIFI_SERVICE) as WifiManager
        return try {
            val method = wifiManager.javaClass.getDeclaredMethod("isWifiApEnabled")
            method.invoke(wifiManager) as? Boolean == true
        } catch (ignored: Throwable) {
            false
        }
    }
}
