package com.example.leximaster.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

object NetworkConnectivityChecker {

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService<ConnectivityManager>() ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        // checking for NET_CAPABILITY_INTERNET is safer than checking specific transports
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getNetworkStatusFlow(context: Context): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // use trySend to safely push values from the callback thread
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        if (connectivityManager != null) {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, networkCallback)
        }

        // 1. Send the initial network state immediately
        trySend(isNetworkAvailable(context))

        // 2. This block keeps the flow active and safely unregisters the listener when collected cancels
        awaitClose {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged() // Prevents emitting the same true/false state multiple times consecutively
}