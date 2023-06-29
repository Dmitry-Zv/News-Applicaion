package by.zharikov.newsapplicaion.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@kotlinx.serialization.Serializable
@Parcelize
data class UiArticle(
    val article: Article,
    var isSave: Boolean
) : Parcelable {
    companion object {
        fun mapFromListOfArticleToListOfUiArticle(
            articles: List<Article>,
            articleSaveState: (title: String?) -> Boolean
        ): List<UiArticle> =
            articles.map {
                UiArticle(
                    article = it,
                    isSave = articleSaveState(it.title)
                )
            }

    }
}
