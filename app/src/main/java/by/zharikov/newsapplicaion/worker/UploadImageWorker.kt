package by.zharikov.newsapplicaion.worker

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import by.zharikov.newsapplicaion.repository.UploadDownloadImageRepository
import by.zharikov.newsapplicaion.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UploadImageWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val data = inputData.getString(Constants.KEY_IMAGE)
        val uri = Uri.parse(data)
        return@withContext try {
            val uploadDownloadImageRepository = UploadDownloadImageRepository(context)
            if (data != null) {
                uploadDownloadImageRepository.uploadImage(uri)
                Result.success()
            } else Result.retry()

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}