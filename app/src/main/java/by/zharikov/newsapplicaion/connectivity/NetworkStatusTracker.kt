package by.zharikov.newsapplicaion.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map


class NetworkStatusTracker(context: Context) {


    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @RequiresApi(Build.VERSION_CODES.O)
    val networkStatus = callbackFlow {

            val networkStatusCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    trySend(NetworkStatus.Available).isSuccess
                }


                override fun onLost(network: Network) {
                    trySend(NetworkStatus.Lost).isSuccess
                }



            }
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkStatusCallback)

            awaitClose {
                connectivityManager.unregisterNetworkCallback(networkStatusCallback)
            }
        }



}

inline fun <Result> Flow<NetworkStatus>.map(
    crossinline onAvailable: suspend () -> Result,
    crossinline onLost: suspend () -> Result
): Flow<Result> = map { status ->
    when (status) {
        NetworkStatus.Available -> onAvailable()
        NetworkStatus.Lost -> onLost()
    }
}