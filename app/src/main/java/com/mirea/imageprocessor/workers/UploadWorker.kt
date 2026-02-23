package com.mirea.imageprocessor.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

class UploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("WorkManager", "UploadWorker: start")

            val inputPath = inputData.getString("watermarked_path") ?: ""

            for (i in 1..100) {
                delay(25) // 2.5 seconds for all processes
                WorkerProgress.updateProgress(i)
                setProgress(workDataOf("progress" to i))
            }

            Log.d("WorkManager", "UploadWorker: success")
            Result.success(workDataOf(
                "uploaded_url" to "cloud.com/photo_watermarked.jpg",
                "file_name" to "photo_watermarked.jpg"
            ))
        } catch (e: Exception) {
            Log.e("WorkManager", "UploadWorker: error", e)
            Result.failure()
        }
    }
}