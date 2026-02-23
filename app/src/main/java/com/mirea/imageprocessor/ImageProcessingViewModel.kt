package com.mirea.imageprocessor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.mirea.imageprocessor.workers.CompressWorker
import com.mirea.imageprocessor.workers.UploadWorker
import com.mirea.imageprocessor.workers.WatermarkWorker
import com.mirea.imageprocessor.workers.WorkerProgress
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProcessingUiState(
    val currentStep: String = "",
    val progress: Int = 0,
    val isProcessing: Boolean = false,
    val result: String? = null,
    val error: String? = null
)

class ImageProcessingViewModel : ViewModel() {

    private val workManager = WorkManager.getInstance()

    private val _uiState = MutableStateFlow(ProcessingUiState())
    val uiState: StateFlow<ProcessingUiState> = _uiState.asStateFlow()

    init {
        // Tracking the progress
        viewModelScope.launch {
            WorkerProgress.progress.collect { progress ->
                _uiState.update { it.copy(progress = progress) }
            }
        }
    }

    fun startProcessing() {
        _uiState.update {
            it.copy(
                isProcessing = true,
                currentStep = "Compressing photo...",
                progress = 0,
                result = null,
                error = null
            )
        }
        WorkerProgress.reset()

        // Creating Worker chains
        val compressWork = OneTimeWorkRequestBuilder<CompressWorker>()
            .setConstraints(Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build())
            .build()

        val watermarkWork = OneTimeWorkRequestBuilder<WatermarkWorker>()
            .build()

        val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>()
            .build()

        // Building the chain
        workManager.beginWith(compressWork)
            .then(watermarkWork)
            .then(uploadWork)
            .enqueue()

        // Status tracking
        viewModelScope.launch {
            workManager.getWorkInfoByIdLiveData(compressWork.id)
                .asFlow()
                .collect { info ->
                    when (info?.state) {
                        WorkInfo.State.RUNNING -> {
                            _uiState.update { it.copy(currentStep = "Compress the photo...") }
                        }
                        WorkInfo.State.FAILED -> {
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    error = "Photo compress error"
                                )
                            }
                        }
                        else -> {}
                    }
                }
        }

        viewModelScope.launch {
            workManager.getWorkInfoByIdLiveData(watermarkWork.id)
                .asFlow()
                .collect { info ->
                    when (info?.state) {
                        WorkInfo.State.RUNNING -> {
                            _uiState.update { it.copy(currentStep = "Adding a watermark...") }
                        }
                        WorkInfo.State.FAILED -> {
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    error = "Adding a watermark error"
                                )
                            }
                        }
                        else -> {}
                    }
                }
        }

        viewModelScope.launch {
            workManager.getWorkInfoByIdLiveData(uploadWork.id)
                .asFlow()
                .collect { info ->
                    when (info?.state) {
                        WorkInfo.State.RUNNING -> {
                            _uiState.update { it.copy(currentStep = "Uploading it to the cloud...") }
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            val fileName = info.outputData.getString("file_name") ?: "photo.jpg"
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    currentStep = "Done!",
                                    result = "Photo uploaded: $fileName"
                                )
                            }
                        }
                        WorkInfo.State.FAILED -> {
                            _uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    error = "Uploading error"
                                )
                            }
                        }
                        else -> {}
                    }
                }
        }
    }

    fun reset() {
        _uiState.update { ProcessingUiState() }
        WorkerProgress.reset()
    }
}