package com.magicalwardrobe

import com.magicalwardrobe.data.repository.ApiKeyValidator
import org.junit.Test
import org.junit.Assert.*

class ApiKeyValidatorTest {
    
    private val validator = ApiKeyValidator()
    
    @Test
    fun `test valid API key format`() {
        val validKey = "AIzaSyBxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
        assertTrue(validator.isApiKeyFormatValid(validKey))
    }
    
    @Test
    fun `test invalid API key format - empty`() {
        assertFalse(validator.isApiKeyFormatValid(""))
    }
    
    @Test
    fun `test invalid API key format - default placeholder`() {
        assertFalse(validator.isApiKeyFormatValid("YOUR_GEMINI_API_KEY"))
    }
    
    @Test
    fun `test invalid API key format - too short`() {
        val shortKey = "short"
        assertFalse(validator.isApiKeyFormatValid(shortKey))
    }
    
    @Test
    fun `test invalid API key format - invalid characters`() {
        val invalidKey = "AIzaSyB@#$%^&*()"
        assertFalse(validator.isApiKeyFormatValid(invalidKey))
    }
}