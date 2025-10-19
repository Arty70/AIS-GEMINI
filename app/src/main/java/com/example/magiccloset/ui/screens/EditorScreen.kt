package com.example.magiccloset.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.magiccloset.ai.AIClient
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(nav: NavController) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val pickedImage: MutableState<Uri?> = remember { mutableStateOf(null) }
    val prompt = remember { mutableStateOf("A classy streetwear outfit") }
    val isGenerating = remember { mutableStateOf(false) }
    val resultImage: MutableState<Uri?> = remember { mutableStateOf(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        pickedImage.value = uri
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Editor") }) }, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { picker.launch("image/*") }) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Text("  Pick photo")
                }
                Button(onClick = { /* camera later */ }) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null)
                    Text("  Camera")
                }
            }

            OutlinedTextField(
                value = prompt.value,
                onValueChange = { prompt.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Style prompt (e.g. 'formal business suit')") }
            )

            if (pickedImage.value != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = rememberAsyncImagePainter(pickedImage.value),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(220.dp)
                    )
                }
            }

            Button(
                onClick = {
                    val source = pickedImage.value
                    if (source == null) {
                        scope.launch { snackbar.showSnackbar("Pick a photo first") }
                        return@Button
                    }
                    scope.launch {
                        isGenerating.value = true
                        try {
                            val uri = AIClient.generateOutfit(uri = source, prompt = prompt.value)
                            resultImage.value = uri
                        } catch (t: Throwable) {
                            snackbar.showSnackbar("Failed to generate: ${'$'}{t.message}")
                        } finally {
                            isGenerating.value = false
                        }
                    }
                }, enabled = !isGenerating.value
            ) {
                Text(if (isGenerating.value) "Generating..." else "Generate outfit")
            }

            if (isGenerating.value) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }

            if (resultImage.value != null) {
                Text("Result")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = rememberAsyncImagePainter(resultImage.value),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(320.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { /* save later */ }) { Text("Save") }
                    Button(onClick = { /* share later */ }) { Text("Share") }
                }
            }
        }
    }
}
