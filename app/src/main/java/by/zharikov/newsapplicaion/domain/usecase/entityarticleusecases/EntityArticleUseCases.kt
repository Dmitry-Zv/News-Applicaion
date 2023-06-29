package by.zharikov.newsapplicaion.domain.usecase.entityarticleusecases

data class EntityArticleUseCases(
    val deleteAllArticlesFromDb: DeleteAllArticlesFromDb,
    val deleteArticleFromDb: DeleteArticleFromDb,
    val insertArticleInDb: InsertArticleInDb,
    val getAllArticlesFromDb: GetAllArticlesFromDb,
    val insertAllArticlesInDb: InsertAllArticlesInDb,
    val getArticleSaveState: GetArticleSaveState
)