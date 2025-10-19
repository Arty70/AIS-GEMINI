package com.magicalwardrobe.data.repository

import android.content.Context
import android.net.Uri
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleCloudStorageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val storage: Storage by lazy {
        StorageOptions.getDefaultInstance().service
    }
    
    suspend fun uploadImage(
        imageUri: Uri,
        fileName: String,
        bucketName: String = "magical-wardrobe-images"
    ): String? = withContext(Dispatchers.IO) {
        try {
            val bucket = getOrCreateBucket(bucketName)
            val blobName = "generated-images/$fileName"
            
            val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext null
            
            val imageData = inputStream.readBytes()
            inputStream.close()
            
            val blob = bucket.create(blobName, imageData)
            blob.mediaLink
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun downloadImage(blobName: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val bucket = storage.get("magical-wardrobe-images")
            val blob = bucket.get(blobName)
            blob?.getContent()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun listImages(prefix: String = "generated-images/"): List<String> = withContext(Dispatchers.IO) {
        try {
            val bucket = storage.get("magical-wardrobe-images")
            val blobs = bucket.list(Storage.BlobListOption.prefix(prefix))
            
            blobs.values.map { it.name }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun deleteImage(blobName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val bucket = storage.get("magical-wardrobe-images")
            val blob = bucket.get(blobName)
            blob?.delete()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private suspend fun getOrCreateBucket(bucketName: String): Bucket = withContext(Dispatchers.IO) {
        try {
            var bucket = storage.get(bucketName)
            if (bucket == null) {
                bucket = storage.create(
                    com.google.cloud.storage.BucketInfo.of(bucketName)
                )
            }
            bucket
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}