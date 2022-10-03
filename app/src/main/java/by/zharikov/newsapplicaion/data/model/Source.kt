package by.zharikov.newsapplicaion.data.model


import androidx.room.ColumnInfo
import androidx.room.Entity
import java.io.Serializable

@Entity(tableName = "source")
data class Source(
    @ColumnInfo(name = "source_id")
    val id: String?,
    val name: String
) : Serializable
