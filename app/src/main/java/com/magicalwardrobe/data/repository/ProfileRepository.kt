package com.magicalwardrobe.data.repository

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor() {
    
    suspend fun getProfileData(): ProfileData {
        // Simulate network delay
        delay(1000)
        return ProfileData(
            totalCreations = 24,
            favoriteCount = 8,
            styleCount = 6
        )
    }
}

data class ProfileData(
    val totalCreations: Int,
    val favoriteCount: Int,
    val styleCount: Int
)