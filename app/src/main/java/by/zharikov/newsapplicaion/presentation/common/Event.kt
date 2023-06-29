package by.zharikov.newsapplicaion.presentation.common

interface Event<E> {

    fun onEvent(event: E)
}