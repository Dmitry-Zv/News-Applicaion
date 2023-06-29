package by.zharikov.newsapplicaion.domain.model


import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize

@Parcelize
@kotlinx.serialization.Serializable
data class Source(
    @ColumnInfo(name = "source_id")
    val id: String?,
    val name: String
) : Parcelable {
    constructor() : this("", "")
}
