package com.mirea.imageprocessor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ImageProcessingScreen(
    onBack: () -> Unit
) {
    val viewModel: ImageProcessingViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Status
        Text(
            text = when {
                uiState.isProcessing -> uiState.currentStep
                uiState.result != null -> "Done!"
                uiState.error != null -> "Error!"
                else -> "Photo processing"
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // SubTitle
        when {
            uiState.isProcessing -> "It may take a few seconds..."
            uiState.result != null -> uiState.result
            uiState.error != null -> uiState.error
            else -> "Click the button to start"
        }?.let {
            Text(
                text = it,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Linear Progress Bar
        if (uiState.isProcessing) {
            LinearProgressIndicator(
                progress = uiState.progress / 100f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${uiState.progress}%",
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Button for start
        Button(
            onClick = { viewModel.startProcessing() },
            enabled = !uiState.isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start processing and uploading")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}