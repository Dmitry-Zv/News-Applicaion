package by.zharikov.newsapplicaion.domain.repository

import android.net.Uri
import by.zharikov.newsapplicaion.utils.Resource

interface ImageRepository {

    suspend fun setImage(data: Uri): Resource<Unit>

    suspend fun getImage(): Resource<ByteArray>

    suspend fun deleteImage(): Resource<Unit>
}