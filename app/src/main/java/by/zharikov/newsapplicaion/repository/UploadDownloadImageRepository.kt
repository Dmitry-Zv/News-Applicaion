package by.zharikov.newsapplicaion.repository

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class UploadDownloadImageRepository(private val context: Context) {

    private val storage = Firebase.storage
    private val auth = Firebase.auth
    val data = MutableLiveData<ByteArray>()


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
        profileImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            Toast.makeText(context, "Download image", Toast.LENGTH_SHORT).show()
            data.value = it
        }.addOnFailureListener {
            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}