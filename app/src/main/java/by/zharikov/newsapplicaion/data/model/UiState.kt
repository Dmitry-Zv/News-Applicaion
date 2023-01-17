package by.zharikov.newsapplicaion.data.model

sealed class UiState() {
    class ShowArticles(val articles: List<Article>) : UiState()
    class Error(val exception: Exception) : UiState()
    object Initial : UiState()
}
