package com.magicalwardrobe.data.repository

import android.content.Context
import android.net.Uri
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val googleApiManager: GoogleApiManager
) {
    
    suspend fun uploadImage(
        imageUri: Uri,
        fileName: String,
        folderName: String = "Magical Wardrobe"
    ): String? = withContext(Dispatchers.IO) {
        try {
            val driveService = googleApiManager.getDriveService() ?: return@withContext null
            
            // Create folder if it doesn't exist
            val folderId = createFolderIfNotExists(folderName, driveService)
            
            // Read image data
            val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext null
            
            val imageData = inputStream.readBytes()
            inputStream.close()
            
            // Create file metadata
            val fileMetadata = File().apply {
                name = fileName
                parents = listOf(folderId)
            }
            
            // Upload file
            val mediaContent = com.google.api.client.http.ByteArrayContent(
                "image/jpeg",
                imageData
            )
            
            val uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
            
            uploadedFile.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun downloadImage(fileId: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val driveService = googleApiManager.getDriveService() ?: return@withContext null
            
            val outputStream = ByteArrayOutputStream()
            driveService.files().get(fileId)
                .executeMediaAndDownloadTo(outputStream)
            
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun listImages(folderName: String = "Magical Wardrobe"): List<File> = withContext(Dispatchers.IO) {
        try {
            val driveService = googleApiManager.getDriveService() ?: return@withContext emptyList()
            
            val folderId = createFolderIfNotExists(folderName, driveService)
            
            val result: FileList = driveService.files().list()
                .setQ("'$folderId' in parents and mimeType contains 'image/'")
                .setFields("files(id,name,createdTime,size)")
                .execute()
            
            result.files ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun deleteImage(fileId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val driveService = googleApiManager.getDriveService() ?: return@withContext false
            
            driveService.files().delete(fileId).execute()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private suspend fun createFolderIfNotExists(folderName: String, driveService: Drive): String = withContext(Dispatchers.IO) {
        try {
            // Search for existing folder
            val result: FileList = driveService.files().list()
                .setQ("name='$folderName' and mimeType='application/vnd.google-apps.folder'")
                .setFields("files(id)")
                .execute()
            
            if (result.files?.isNotEmpty() == true) {
                return@withContext result.files!![0].id
            }
            
            // Create folder if it doesn't exist
            val folderMetadata = File().apply {
                name = folderName
                mimeType = "application/vnd.google-apps.folder"
            }
            
            val folder = driveService.files().create(folderMetadata)
                .setFields("id")
                .execute()
            
            folder.id
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}