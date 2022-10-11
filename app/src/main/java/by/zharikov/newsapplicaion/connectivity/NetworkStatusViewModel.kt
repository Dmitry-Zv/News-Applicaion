package by.zharikov.newsapplicaion.connectivity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

sealed class MyState {
    object Fetched : MyState()
    object Lost : MyState()
}

class NetworkStatusViewModel(networkStatusTracker: NetworkStatusTracker) : ViewModel() {

    @OptIn(FlowPreview::class)
    @RequiresApi(Build.VERSION_CODES.O)
    val state = networkStatusTracker.networkStatus.map(
        onAvailable = { MyState.Fetched },
        onLost = { MyState.Lost }
    ).debounce(500).asLiveData(Dispatchers.IO)

}