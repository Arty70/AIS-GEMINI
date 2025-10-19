package com.magicalwardrobe.data.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val googleApiManager: GoogleApiManager
) {
    
    suspend fun signIn(): GoogleSignInAccount? = withContext(Dispatchers.IO) {
        try {
            val signInClient = googleApiManager.initializeGoogleSignIn()
            val signInIntent = signInClient.signInIntent
            
            // In a real implementation, you would handle the sign-in result
            // This is a simplified version for demonstration
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            val signInClient = googleApiManager.getGoogleSignInClient()
            signInClient?.signOut()
            googleApiManager.clearServices()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun getCurrentUser(): GoogleSignInAccount? = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            account
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun isSignedIn(): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            account != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            account?.idToken
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun createCredential(): GoogleCredential? = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken != null) {
                GoogleCredential().setAccessToken(accessToken)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}