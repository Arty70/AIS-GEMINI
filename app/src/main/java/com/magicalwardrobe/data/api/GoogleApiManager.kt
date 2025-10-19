package com.magicalwardrobe.data.api

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.photos.library.v1.PhotosLibraryClient
import com.google.photos.library.v1.PhotosLibrarySettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleApiManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var googleSignInClient: GoogleSignInClient? = null
    private var driveService: Drive? = null
    private var photosLibraryClient: PhotosLibraryClient? = null
    
    fun initializeGoogleSignIn(): GoogleSignInClient {
        if (googleSignInClient == null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .requestScopes(Scope("https://www.googleapis.com/auth/photoslibrary.readonly"))
                .build()
            
            googleSignInClient = GoogleSignIn.getClient(context, gso)
        }
        return googleSignInClient!!
    }
    
    suspend fun initializeDriveService(credential: GoogleCredential): Drive = withContext(Dispatchers.IO) {
        if (driveService == null) {
            driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("Magical Wardrobe")
                .build()
        }
        driveService!!
    }
    
    suspend fun initializePhotosLibraryClient(accessToken: String): PhotosLibraryClient = withContext(Dispatchers.IO) {
        if (photosLibraryClient == null) {
            val settings = PhotosLibrarySettings.newBuilder()
                .setCredentialsProvider { accessToken }
                .build()
            
            photosLibraryClient = PhotosLibraryClient.initialize(settings)
        }
        photosLibraryClient!!
    }
    
    fun getGoogleSignInClient(): GoogleSignInClient? = googleSignInClient
    fun getDriveService(): Drive? = driveService
    fun getPhotosLibraryClient(): PhotosLibraryClient? = photosLibraryClient
    
    fun clearServices() {
        googleSignInClient = null
        driveService = null
        photosLibraryClient = null
    }
}