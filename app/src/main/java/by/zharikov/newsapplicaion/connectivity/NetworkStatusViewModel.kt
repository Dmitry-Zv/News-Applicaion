package by.zharikov.newsapplicaion.connectivity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import javax.inject.Inject

sealed class MyState {
    object Fetched : MyState()
    object Lost : MyState()
}

@HiltViewModel
class NetworkStatusViewModel @Inject constructor(networkStatusTracker: NetworkStatusTracker) :
    ViewModel() {

    @OptIn(FlowPreview::class)
    @RequiresApi(Build.VERSION_CODES.O)
    val state = networkStatusTracker.networkStatus.map(
        onAvailable = { MyState.Fetched },
        onLost = { MyState.Lost }
    ).debounce(700)

}