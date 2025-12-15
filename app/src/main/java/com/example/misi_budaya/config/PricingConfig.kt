package com.example.misi_budaya.config

/**
 * Konfigurasi harga dan pricing untuk premium membership
 */
object PricingConfig {
    
    // Premium Membership Price (dalam Rupiah)
    const val PREMIUM_PRICE_CENTS: Long = 49999L  // Rp 49.999
    
    // Premium Membership Price (dalam Rupiah untuk display)
    const val PREMIUM_PRICE_IDR: Long = 49999
    
    // Premium description
    const val PREMIUM_DESCRIPTION = "Upgrade Premium"
    
    /**
     * Get formatted price display
     * @return String format "Rp X,XXX"
     */
    fun getFormattedPrice(): String {
        return "Rp ${String.format("%,d", PREMIUM_PRICE_IDR)}"
    }
    
    /**
     * Get price for payment gateway (dalam Rupiah)
     */
    fun getPriceInRupiah(): Long = PREMIUM_PRICE_CENTS
}

