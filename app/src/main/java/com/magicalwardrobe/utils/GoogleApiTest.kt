package com.magicalwardrobe.utils

import android.content.Context
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object GoogleApiTest {
    
    fun testGooglePlayServices(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        
        return when (resultCode) {
            ConnectionResult.SUCCESS -> true
            else -> {
                val errorMessage = googleApiAvailability.getErrorString(resultCode)
                showTestResult(context, false, "Google Play Services: $errorMessage")
                false
            }
        }
    }
    
    fun testGoogleSignIn(context: Context): Boolean {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            val isSignedIn = account != null
            showTestResult(
                context, 
                isSignedIn, 
                if (isSignedIn) "Google Sign-In активен" else "Google Sign-In не активен"
            )
            isSignedIn
        } catch (e: Exception) {
            showTestResult(context, false, "Ошибка Google Sign-In: ${e.message}")
            false
        }
    }
    
    fun testGoogleDriveAccess(context: Context): Boolean {
        return try {
            // Test if Google Drive is accessible
            val account = GoogleSignIn.getLastSignedInAccount(context)
            val hasDriveScope = account?.grantedScopes?.any { 
                it.scopeUri.contains("drive") 
            } ?: false
            
            showTestResult(
                context,
                hasDriveScope,
                if (hasDriveScope) "Google Drive доступен" else "Google Drive недоступен"
            )
            hasDriveScope
        } catch (e: Exception) {
            showTestResult(context, false, "Ошибка Google Drive: ${e.message}")
            false
        }
    }
    
    fun testGooglePhotosAccess(context: Context): Boolean {
        return try {
            // Test if Google Photos is accessible
            val account = GoogleSignIn.getLastSignedInAccount(context)
            val hasPhotosScope = account?.grantedScopes?.any { 
                it.scopeUri.contains("photos") 
            } ?: false
            
            showTestResult(
                context,
                hasPhotosScope,
                if (hasPhotosScope) "Google Photos доступен" else "Google Photos недоступен"
            )
            hasPhotosScope
        } catch (e: Exception) {
            showTestResult(context, false, "Ошибка Google Photos: ${e.message}")
            false
        }
    }
    
    fun runAllTests(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            val results = listOf(
                "Google Play Services" to testGooglePlayServices(context),
                "Google Sign-In" to testGoogleSignIn(context),
                "Google Drive" to testGoogleDriveAccess(context),
                "Google Photos" to testGooglePhotosAccess(context)
            )
            
            val successCount = results.count { it.second }
            val totalCount = results.size
            
            showTestResult(
                context,
                successCount == totalCount,
                "Тесты завершены: $successCount/$totalCount успешно"
            )
        }
    }
    
    private fun showTestResult(context: Context, success: Boolean, message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(
                context,
                if (success) "✅ $message" else "❌ $message",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}