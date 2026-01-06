package com.jeruk.alp_frontend.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    
    // Conversion rates (base: IDR)
    private const val USD_RATE = 15000.0
    private const val EUR_RATE = 16500.0
    private const val JPY_RATE = 110.0
    private const val GBP_RATE = 19000.0
    
    /**
     * Format harga sesuai dengan currency yang dipilih
     * @param amountInIDR Harga dalam Rupiah (dari database)
     * @param currencyCode Kode mata uang (IDR, USD, EUR, JPY, GBP)
     * @return String formatted price dengan symbol
     */
    fun formatPrice(amountInIDR: Double, currencyCode: String = "IDR"): String {
        return when (currencyCode) {
            "USD" -> {
                val amountInUSD = amountInIDR / USD_RATE
                "$${String.format("%.2f", amountInUSD)}"
            }
            "EUR" -> {
                val amountInEUR = amountInIDR / EUR_RATE
                "€${String.format("%.2f", amountInEUR)}"
            }
            "JPY" -> {
                val amountInJPY = amountInIDR / JPY_RATE
                "¥${String.format("%.0f", amountInJPY)}"
            }
            "GBP" -> {
                val amountInGBP = amountInIDR / GBP_RATE
                "£${String.format("%.2f", amountInGBP)}"
            }
            else -> { // IDR (default)
                "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(amountInIDR.toLong())}"
            }
        }
    }
    
    /**
     * Format harga untuk Int (backward compatibility)
     */
    fun formatPrice(amountInIDR: Int, currencyCode: String = "IDR"): String {
        return formatPrice(amountInIDR.toDouble(), currencyCode)
    }
    
    /**
     * Get currency symbol only
     */
    fun getCurrencySymbol(currencyCode: String): String {
        return when (currencyCode) {
            "USD" -> "$"
            "EUR" -> "€"
            "JPY" -> "¥"
            "GBP" -> "£"
            else -> "Rp"
        }
    }
    
    /**
     * Get currency name
     */
    fun getCurrencyName(currencyCode: String): String {
        return when (currencyCode) {
            "USD" -> "US Dollar"
            "EUR" -> "Euro"
            "JPY" -> "Japanese Yen"
            "GBP" -> "British Pound"
            else -> "Indonesian Rupiah"
        }
    }
}
