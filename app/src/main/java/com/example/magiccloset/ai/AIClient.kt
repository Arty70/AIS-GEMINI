package com.example.magiccloset.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object AIClient {
    // Placeholder mock: returns the input image URI after a fake delay, simulating generation
    @WorkerThread
    suspend fun generateOutfit(uri: Uri, prompt: String): Uri = withContext(Dispatchers.IO) {
        // TODO: replace with Gemini image editing call. For now, simulate latency
        delay(1200)
        return@withContext uri
    }

    // Placeholder 4K upscale: just re-save as PNG with large size in name
    @WorkerThread
    suspend fun upscaleTo4K(context: Context, input: Uri): Uri = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(input) ?: error("Cannot open input")
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val width = original.width
        val height = original.height
        if (width <= 0 || height <= 0) error("Invalid image")

        // Target: longer edge = 3840 (approx 4K UHD), maintain aspect ratio
        val isLandscape = width >= height
        val targetLong = 3840
        val scale = if (isLandscape) targetLong.toFloat() / width.toFloat() else targetLong.toFloat() / height.toFloat()

        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)

        val upscaled = if (scale > 0f && (targetWidth != width || targetHeight != height))
            Bitmap.createScaledBitmap(original, targetWidth, targetHeight, true)
        else original

        val file = File(context.cacheDir, "result_4k_${'$'}{UUID.randomUUID()}.png")
        FileOutputStream(file).use { out ->
            upscaled.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        if (upscaled !== original) original.recycle()
        Uri.fromFile(file)
    }
}
