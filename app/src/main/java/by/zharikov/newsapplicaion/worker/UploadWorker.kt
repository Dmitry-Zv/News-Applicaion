package by.zharikov.newsapplicaion.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.repository.UploadDownloadUiArticleOnFirebaseDatabaseRepository
import by.zharikov.newsapplicaion.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class UploadWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            showNotification()
            val uploadDownloadUiArticleOnFirebaseDatabaseRepository =
                UploadDownloadUiArticleOnFirebaseDatabaseRepository(context)
            uploadDownloadUiArticleOnFirebaseDatabaseRepository.saveDataToFirebase()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }


    }


    private suspend fun showNotification() {
        setForeground(
            ForegroundInfo(
                Random.nextInt(),
                NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_upload_24)
                    .setContentTitle("Upload!")
                    .setContentText("Upload in progress...")
                    .build()
            )
        )
    }

}