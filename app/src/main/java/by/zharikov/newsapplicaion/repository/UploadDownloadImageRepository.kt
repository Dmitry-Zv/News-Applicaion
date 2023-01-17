package by.zharikov.newsapplicaion.repository

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


sealed class ResultDownload {
    class Success(val data: ByteArray) : ResultDownload()
    class UnSuccess(val error: Exception) : ResultDownload()
    object Initial : ResultDownload()
}

class UploadDownloadImageRepository(private val context: Context) {

    private val storage = Firebase.storage
    private val auth = Firebase.auth
    private val _data = MutableStateFlow<ResultDownload>(ResultDownload.Initial)
    val data = _data.asStateFlow()


    fun uploadImage(data: Uri) {
        val storageRef = storage.reference
        val profileImagesRef = storageRef.child("images/${auth.currentUser?.uid}/profile.jpg")
        val uploadTask = profileImagesRef.putFile(data)
        uploadTask.addOnSuccessListener {
            Toast.makeText(context, "Add to store", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }

    }

    fun downloadImage() {

        val storageRef = storage.reference
        val profileImageRef = storageRef.child("images/${auth.currentUser?.uid}/profile.jpg")
        val ONE_MEGABYTE: Long = 1024 * 1024

        profileImageRef.getBytes(ONE_MEGABYTE).addOnCompleteListener { task ->

            if (task.isSuccessful) _data.value = ResultDownload.Success(data = task.result)
            else _data.value = ResultDownload.UnSuccess(error = Exception("Don't download!"))
        }
    }


    fun deleteImage() {

        val storageRef = storage.reference
        val profileImageRef = storageRef.child("images/${auth.currentUser?.uid}/profile.jpg")
        profileImageRef.delete().addOnSuccessListener {
            Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }


    }
}