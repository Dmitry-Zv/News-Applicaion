package by.zharikov.newsapplicaion.data.model

import by.zharikov.newsapplicaion.R

data class TagModel(
    val tagName: String,
    val imageInt: Int
)

object TagModels {
    val tagModelList = listOf<TagModel>(
        TagModel("Business", R.drawable.buisness),
        TagModel("Entertainment", R.drawable.entertainment),
        TagModel("General", R.drawable.general),
        TagModel("Health", R.drawable.health),
        TagModel("Science", R.drawable.science),
        TagModel("Sports", R.drawable.sports),
        TagModel("Technology", R.drawable.technologies),
    )
}