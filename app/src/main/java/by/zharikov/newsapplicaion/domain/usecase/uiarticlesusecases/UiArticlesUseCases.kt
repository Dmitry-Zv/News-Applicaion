package by.zharikov.newsapplicaion.domain.usecase.uiarticlesusecases

data class UiArticlesUseCases(
    val getUiArticlesFromFirebase: GetUiArticlesFromFirebase,
    val saveUiArticlesToFirebase: SaveUiArticlesToFirebase,
    val deleteUiArticleFromFirebase: DeleteUiArticleFromFirebase
)