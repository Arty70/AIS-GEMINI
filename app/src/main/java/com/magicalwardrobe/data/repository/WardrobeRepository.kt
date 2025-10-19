package com.magicalwardrobe.data.repository

import com.magicalwardrobe.ui.screens.wardrobe.WardrobeItem
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WardrobeRepository @Inject constructor() {
    
    suspend fun getAllWardrobeItems(): List<WardrobeItem> {
        // Simulate network delay
        delay(1000)
        return getWardrobeItems()
    }
    
    suspend fun toggleFavorite(itemId: String) {
        // Simulate API call
        delay(500)
        // In real implementation, this would update the database
    }
    
    suspend fun deleteWardrobeItem(itemId: String) {
        // Simulate API call
        delay(500)
        // In real implementation, this would delete from database
    }
    
    private fun getWardrobeItems(): List<WardrobeItem> {
        return listOf(
            WardrobeItem("Деловой костюм", "Деловой", true, Pair(com.magicalwardrobe.ui.theme.MagicalPurple, com.magicalwardrobe.ui.theme.MagicalBlue)),
            WardrobeItem("Вечернее платье", "Вечерний", false, Pair(com.magicalwardrobe.ui.theme.MagicalPink, com.magicalwardrobe.ui.theme.MagicalGold)),
            WardrobeItem("Джинсы и футболка", "Повседневный", true, Pair(com.magicalwardrobe.ui.theme.MagicalBlue, com.magicalwardrobe.ui.theme.MagicalPurple)),
            WardrobeItem("Спортивный костюм", "Спортивный", false, Pair(com.magicalwardrobe.ui.theme.MagicalGold, com.magicalwardrobe.ui.theme.MagicalPink)),
            WardrobeItem("Романтичное платье", "Романтичный", true, Pair(com.magicalwardrobe.ui.theme.MagicalPink, com.magicalwardrobe.ui.theme.MagicalPurple)),
            WardrobeItem("Креативная рубашка", "Креативный", false, Pair(com.magicalwardrobe.ui.theme.MagicalPurple, com.magicalwardrobe.ui.theme.MagicalGold))
        )
    }
}