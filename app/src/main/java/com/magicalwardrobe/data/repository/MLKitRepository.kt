package com.magicalwardrobe.data.repository

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MLKitRepository @Inject constructor() {
    
    suspend fun detectFaces(bitmap: Bitmap): List<Face> = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .build()
            
            val detector = FaceDetection.getClient(options)
            
            val result = detector.process(image).get()
            detector.close()
            
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun labelImage(bitmap: Bitmap): List<ImageLabel> = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val labeler = ImageLabeling.getClient()
            
            val result = labeler.process(image).get()
            labeler.close()
            
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun detectObjects(bitmap: Bitmap): List<DetectedObject> = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val options = ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .build()
            
            val detector = ObjectDetection.getClient(options)
            
            val result = detector.process(image).get()
            detector.close()
            
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun analyzeImageForClothing(bitmap: Bitmap): ClothingAnalysis = withContext(Dispatchers.IO) {
        try {
            val labels = labelImage(bitmap)
            val objects = detectObjects(bitmap)
            val faces = detectFaces(bitmap)
            
            val clothingItems = mutableListOf<String>()
            val colors = mutableListOf<String>()
            val styles = mutableListOf<String>()
            
            // Analyze labels for clothing
            labels.forEach { label ->
                val text = label.text.lowercase()
                when {
                    text.contains("shirt") || text.contains("blouse") -> clothingItems.add("Рубашка")
                    text.contains("dress") -> clothingItems.add("Платье")
                    text.contains("pants") || text.contains("jeans") -> clothingItems.add("Брюки")
                    text.contains("skirt") -> clothingItems.add("Юбка")
                    text.contains("jacket") || text.contains("coat") -> clothingItems.add("Пиджак")
                    text.contains("suit") -> clothingItems.add("Костюм")
                    text.contains("shoes") || text.contains("boots") -> clothingItems.add("Обувь")
                    text.contains("hat") || text.contains("cap") -> clothingItems.add("Головной убор")
                }
                
                // Analyze colors
                when {
                    text.contains("black") -> colors.add("Черный")
                    text.contains("white") -> colors.add("Белый")
                    text.contains("red") -> colors.add("Красный")
                    text.contains("blue") -> colors.add("Синий")
                    text.contains("green") -> colors.add("Зеленый")
                    text.contains("yellow") -> colors.add("Желтый")
                    text.contains("pink") -> colors.add("Розовый")
                    text.contains("purple") -> colors.add("Фиолетовый")
                }
                
                // Analyze styles
                when {
                    text.contains("formal") || text.contains("business") -> styles.add("Деловой")
                    text.contains("casual") -> styles.add("Повседневный")
                    text.contains("elegant") || text.contains("evening") -> styles.add("Вечерний")
                    text.contains("sport") || text.contains("athletic") -> styles.add("Спортивный")
                    text.contains("vintage") -> styles.add("Винтаж")
                    text.contains("modern") -> styles.add("Современный")
                }
            }
            
            ClothingAnalysis(
                clothingItems = clothingItems.distinct(),
                colors = colors.distinct(),
                styles = styles.distinct(),
                hasPerson = faces.isNotEmpty(),
                confidence = labels.maxOfOrNull { it.confidence } ?: 0f
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ClothingAnalysis()
        }
    }
}

data class ClothingAnalysis(
    val clothingItems: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val styles: List<String> = emptyList(),
    val hasPerson: Boolean = false,
    val confidence: Float = 0f
)