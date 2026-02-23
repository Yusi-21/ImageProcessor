package com.mirea.imageprocessor.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object WorkerProgress {
    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress

    fun updateProgress(value: Int) {
        _progress.value = value
    }

    fun reset() {
        _progress.value = 0
    }
}

class CompressWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("WorkManager", "CompressWorker: start")

            for (i in 1..100) {
                delay(30) // 3 seconds for all processes
                WorkerProgress.updateProgress(i)
                setProgress(workDataOf("progress" to i))
            }

            Log.d("WorkManager", "CompressWorker: success")
            Result.success(workDataOf("compressed_path" to "photo_compressed.jpg"))
        } catch (e: Exception) {
            Log.e("WorkManager", "CompressWorker: error", e)
            Result.failure()
        }
    }
}