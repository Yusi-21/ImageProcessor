package com.mirea.imageprocessor.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

class WatermarkWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("WorkManager", "WatermarkWorker: start")

            val inputPath = inputData.getString("compressed_path") ?: ""

            for (i in 1..100) {
                delay(20) // 2 seconds for all processes
                WorkerProgress.updateProgress(i)
                setProgress(workDataOf("progress" to i))
            }

            Log.d("WorkManager", "WatermarkWorker: success")
            Result.success(workDataOf("watermarked_path" to "photo_watermarked.jpg"))
        } catch (e: Exception) {
            Log.e("WorkManager", "WatermarkWorker: error", e)
            Result.failure()
        }
    }
}