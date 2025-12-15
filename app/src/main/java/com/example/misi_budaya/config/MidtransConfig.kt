package com.example.misi_budaya.config

/**
 * Konfigurasi Midtrans Payment Gateway
 * 
 * Langkah-langkah setup:
 * 1. Daftar ke https://dashboard.sandbox.midtrans.com
 * 2. Login dan buka Settings → Access Keys
 * 3. Copy Client Key
 * 4. Paste ke CLIENT_KEY di bawah
 * 
 * Environment:
 * - SANDBOX: Untuk testing (gunakan test credentials)
 * - PRODUCTION: Untuk live (gunakan production credentials)
 */
object MidtransConfig {
    // ⚠️ GANTI INI DENGAN CLIENT KEY ANDA
    const val CLIENT_KEY = "Mid-client-V307niVfreMMDGYv"
    
    // ⚠️ OPTIONAL: Server Key untuk backend calls (jangan hardcode di production)
    const val SERVER_KEY = "Mid-server-lDrfLVyn-HnhXXNAnBWXel6u"
    
    // Environment: "sandbox" atau "production"
    const val ENVIRONMENT = "sandbox"
    
    // Base URLs
    const val SANDBOX_BASE_URL = "https://app.sandbox.midtrans.com"
    const val PRODUCTION_BASE_URL = "https://app.midtrans.com"
    
    // API Endpoints
    const val SANDBOX_API_URL = "https://app.sandbox.midtrans.com/snap/v1/transactions"
    const val PRODUCTION_API_URL = "https://app.midtrans.com/snap/v1/transactions"
    
    // Get active configuration berdasarkan environment
    fun getBaseUrl(): String = if (ENVIRONMENT == "sandbox") SANDBOX_BASE_URL else PRODUCTION_BASE_URL
    fun getApiUrl(): String = if (ENVIRONMENT == "sandbox") SANDBOX_API_URL else PRODUCTION_API_URL
    
    /**
     * Validate configuration
     */
    fun isConfigured(): Boolean {
        return CLIENT_KEY.isNotEmpty() && !CLIENT_KEY.contains("your", ignoreCase = true) && !CLIENT_KEY.contains("xxx", ignoreCase = true)
    }
}
