package com.example.itemmanagement.utils

import android.content.Context
import android.content.SharedPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

@Serializable
data class ExchangeRateResponse(
    val result: String,
    val provider: String,
    val documentation: String,
    val terms_of_use: String,
    val time_last_update_unix: Long,
    val time_last_update_utc: String,
    val time_next_update_unix: Long,
    val time_next_update_utc: String,
    val time_eol_unix: Long,
    val base_code: String,
    val rates: Map<String, Double>
)

class CurrencyConverter(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("currency_prefs", Context.MODE_PRIVATE)
    private val CACHE_KEY_PREFIX = "rate_"
    private val LAST_UPDATE_KEY = "last_update_time"
    private val CACHE_DURATION_MS = TimeUnit.HOURS.toMillis(24) // 24 hours cache

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    // Default base currency is CNY
    private var baseCurrencyCode: String
        get() = prefs.getString("base_currency", "CNY") ?: "CNY"
        set(value) = prefs.edit().putString("base_currency", value).apply()

    fun setBaseCurrency(currencyCode: String) {
        baseCurrencyCode = currencyCode
    }

    fun getBaseCurrency(): String = baseCurrencyCode

    suspend fun convert(amount: Double, fromCurrency: String, toCurrency: String = baseCurrencyCode): Double {
        if (fromCurrency == toCurrency) return amount
        
        val rate = getExchangeRate(fromCurrency, toCurrency)
        return amount * rate
    }

    private suspend fun getExchangeRate(from: String, to: String): Double {
        val lastUpdate = prefs.getLong(LAST_UPDATE_KEY, 0)
        val now = System.currentTimeMillis()
        
        // If cache is expired, fetch new rates relative to USD
        if (now - lastUpdate > CACHE_DURATION_MS || !prefs.contains("${CACHE_KEY_PREFIX}USD")) {
            fetchAndCacheRates("USD")
        }
        
        // Compute cross rate
        val rateFrom = prefs.getFloat("$CACHE_KEY_PREFIX$from", -1f)
        val rateTo = prefs.getFloat("$CACHE_KEY_PREFIX$to", -1f)
        
        if (rateFrom != -1f && rateTo != -1f) {
            // 1 USD = rateFrom FROM, 1 USD = rateTo TO
            // So 1 FROM = rateTo / rateFrom TO
            return (rateTo / rateFrom).toDouble()
        }
        
        // If still not found (e.g. offline and no cache), return 1.0 as fallback
        return 1.0
    }

    private suspend fun fetchAndCacheRates(baseCode: String) {
        withContext(Dispatchers.IO) {
            try {
                // Using a free API: https://open.er-api.com/v6/latest/USD
                val response: ExchangeRateResponse = client.get("https://open.er-api.com/v6/latest/$baseCode").body()
                
                val editor = prefs.edit()
                response.rates.forEach { (currency, rate) ->
                    editor.putFloat("$CACHE_KEY_PREFIX$currency", rate.toFloat())
                }
                editor.putLong(LAST_UPDATE_KEY, System.currentTimeMillis())
                editor.apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
