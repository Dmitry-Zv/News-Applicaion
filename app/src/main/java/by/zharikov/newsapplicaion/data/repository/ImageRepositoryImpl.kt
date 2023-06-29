package by.zharikov.newsapplicaion.data.repository

import android.net.Uri
import by.zharikov.newsapplicaion.domain.repository.ImageRepository
import by.zharikov.newsapplicaion.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject


class ImageRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val storageRef: StorageReference
) : ImageRepository {


    override suspend fun setImage(data: Uri): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                auth.currentUser?.let {
                    val profileImagesRef = storageRef.child("images/${it.uid}/profile.jpg")
                    profileImagesRef.putFile(data).await()
                    Resource.Success(data = Unit)
                } ?: Resource.Error("Current user is null")

            } catch (e: Exception) {
                Resource.Error(msg = e.message ?: "Unknown error...")
            }
        }
    }

    override suspend fun getImage(): Resource<ByteArray> {
        return withContext(Dispatchers.IO) {
            try {
                auth.currentUser?.let {
                    val profileImageRef = storageRef.child("images/${it.uid}/profile.jpg")
                    val ONE_MEGABYTE: Long = 1024 * 1024
                    val data = profileImageRef.getBytes(ONE_MEGABYTE).await()
                    Resource.Success(data = data)
                } ?: Resource.Error(msg = "Current user is null")
            } catch (e: Exception) {
                Resource.Error(msg = e.message ?: "Unknown error...")
            }
        }
    }


    override suspend fun deleteImage(): Resource<Unit> {

        return withContext(Dispatchers.IO) {
            try {
                auth.currentUser?.let {
                    val profileImageRef = storageRef.child("images/${it.uid}/profile.jpg")
                    profileImageRef.delete().await()
                    Resource.Success(data = Unit)
                } ?: Resource.Error(msg = "Current user is null")
            } catch (e: Exception) {
                Resource.Error(msg = e.message ?: "Unknown error...")
            }
        }
    }
}