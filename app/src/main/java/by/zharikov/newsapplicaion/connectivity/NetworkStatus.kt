package by.zharikov.newsapplicaion.connectivity

sealed class NetworkStatus {
    object Available : NetworkStatus()
    object Lost : NetworkStatus()
}