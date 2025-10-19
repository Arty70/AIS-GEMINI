package com.example.magiccloset.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object AIClient {
    // Placeholder mock: returns the input image URI after a fake delay, simulating generation
    @WorkerThread
    suspend fun generateOutfit(uri: Uri, prompt: String): Uri = withContext(Dispatchers.IO) {
        // TODO: replace with Gemini image editing call. For now, copy image to cache as result
        Thread.sleep(1200)
        return@withContext uri
    }

    // Placeholder 4K upscale: just re-save as PNG with large size in name
    @WorkerThread
    suspend fun upscaleTo4K(context: Context, input: Uri): Uri = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(input) ?: error("Cannot open input")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        val file = File(context.cacheDir, "result_4k_${'$'}{UUID.randomUUID()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        Uri.fromFile(file)
    }
}
