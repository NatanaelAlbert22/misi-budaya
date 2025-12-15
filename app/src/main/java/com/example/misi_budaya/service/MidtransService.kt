package com.example.misi_budaya.service

import android.content.Context
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.misi_budaya.config.MidtransConfig
import com.example.misi_budaya.data.model.MidtransResponse
import com.example.misi_budaya.data.model.TransactionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import android.util.Base64
import org.json.JSONObject

class MidtransService(context: Context) {
    
    companion object {
        private const val TAG = "MidtransService"
    }
    
    private val context = context
    private val clientKey = MidtransConfig.CLIENT_KEY
    private val serverKey = MidtransConfig.SERVER_KEY
    private val baseUrl = MidtransConfig.getBaseUrl()
    private val apiUrl = MidtransConfig.getApiUrl()
    
    /**
     * Generate Snap token untuk pembayaran via REST API
     * @param request TransactionRequest dengan detail pembayaran
     * @return MidtransResponse berisi token dan informasi transaksi
     */
    suspend fun generateSnapToken(request: TransactionRequest): Result<MidtransResponse> = 
        withContext(Dispatchers.IO) {
            return@withContext try {
                Log.d(TAG, "Starting token generation for order")
                val orderId = generateOrderId()
                Log.d(TAG, "Generated Order ID: $orderId")
                
                val requestBody = buildSnapRequest(
                    orderId = orderId,
                    amount = request.amount,
                    email = request.email,
                    firstName = request.firstName,
                    lastName = request.lastName ?: "",
                    phone = request.phone ?: "",
                    itemDescription = request.description
                )
                Log.d(TAG, "Request Body: $requestBody")
                
                // Call Midtrans API untuk generate token
                val response = callMidtransAPI(requestBody)
                Log.d(TAG, "API Response: $response")
                
                if (response.isNotEmpty()) {
                    try {
                        val jsonResponse = JSONObject(response)
                        val token = jsonResponse.optString("token", "")

                        val trueRedirectUrl = jsonResponse.optString("redirect_url", "")
                        
                        if (token.isNotEmpty()) {
                            Result.success(
                                MidtransResponse(
                                    transactionId = request.userId,
                                    orderId = orderId,
                                    token = token,
                                    redirectUrl = trueRedirectUrl
                                )
                            )
                        } else {
                            Log.e(TAG, "No token in response: $response")
                            Result.failure(Exception("No token received from Midtrans"))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing response JSON", e)
                        Result.failure(e)
                    }
                } else {
                    Log.e(TAG, "Empty response from API")
                    Result.failure(Exception("Empty response from Midtrans API"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating Snap token", e)
                e.printStackTrace()
                Result.failure(e)
            }
        }
    
    /**
     * Call Midtrans API untuk generate token
     */
    private fun callMidtransAPI(requestBody: String): String {
        return try {
            Log.d(TAG, "Calling Midtrans API: $apiUrl")
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            
            // Setup request
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            
            // Add Basic Auth header dengan Server Key (username: SERVER_KEY, password: empty)
            val authString = "$serverKey:"
            val auth = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)
            connection.setRequestProperty("Authorization", "Basic $auth")
            
            Log.d(TAG, "Authorization header set with SERVER_KEY: ${serverKey.take(10)}...")
            
            connection.doOutput = true
            connection.doInput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            
            // Write request body
            Log.d(TAG, "Writing request body")
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody)
                writer.flush()
            }
            
            // Read response
            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")
            
            val inputStream = if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                connection.inputStream
            } else {
                Log.e(TAG, "Error response code: $responseCode")
                connection.errorStream
            }
            
            val response = BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
            
            Log.d(TAG, "Raw response: $response")
            response
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception in callMidtransAPI", e)
            e.printStackTrace()
            ""
        }
    }
    
    /**
     * Build Midtrans Snap request body
     */
    private fun buildSnapRequest(
        orderId: String,
        amount: Long,
        email: String,
        firstName: String,
        lastName: String,
        phone: String,
        itemDescription: String
    ): String {
        return """
        {
            "transaction_details": {
                "order_id": "$orderId",
                "gross_amount": $amount
            },
            "customer_details": {
                "first_name": "$firstName",
                "last_name": "$lastName",
                "email": "$email",
                "phone": "$phone"
            },
            "item_details": [
                {
                    "id": "$orderId",
                    "price": $amount,
                    "quantity": 1,
                    "name": "$itemDescription"
                }
            ]
        }
        """.trimIndent()
    }
    
    /**
     * Get status transaksi dari Midtrans
     * @param orderId Order ID dari Midtrans
     * @return Status transaksi
     */
    suspend fun getTransactionStatus(orderId: String): Result<String> = 
        withContext(Dispatchers.IO) {
            return@withContext try {
                // Implementasi ini memerlukan backend untuk query status
                Result.success("pending")
            } catch (e: Exception) {
                Log.e(TAG, "Error getting transaction status", e)
                Result.failure(e)
            }
        }
    
    /**
     * Generate unique order ID
     */
    private fun generateOrderId(): String {
        return "ORD-${System.currentTimeMillis()}-${(Math.random() * 10000).toInt()}"
    }
    
    /**
     * Setup WebView untuk Midtrans Snap
     * Mendeteksi payment completion melalui URL redirect dari Midtrans
     */
    fun setupWebViewForPayment(
        webView: WebView,
        snapUrl: String,
        onPaymentSuccess: (String) -> Unit,
        onPaymentError: (String) -> Unit
    ) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
        }
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "Page loaded: $url")
                
                // Check if this is a Midtrans finish page or redirect after payment
                if (url != null) {
                    when {
                        // Check for success redirect or finish page
                        url.contains("status=success", ignoreCase = true) ||
                        url.contains("status=completed", ignoreCase = true) ||
                        url.contains("finish", ignoreCase = true) -> {
                            Log.d(TAG, "Payment success detected from URL: $url")
                            onPaymentSuccess("payment_success")
                        }
                        // Check for pending payment
                        url.contains("status=pending", ignoreCase = true) -> {
                            Log.d(TAG, "Payment pending: $url")
                            // Still waiting for payment confirmation
                        }
                        // Check for failure
                        url.contains("status=error", ignoreCase = true) ||
                        url.contains("status=failure", ignoreCase = true) ||
                        url.contains("error", ignoreCase = true) -> {
                            Log.e(TAG, "Payment error detected: $url")
                            onPaymentError("Payment failed or was cancelled")
                        }
                    }
                }
            }
            
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e(TAG, "WebView Error Code: $errorCode, Description: $description")
                // Don't immediately call onError for all cases, only for critical errors
                if (errorCode != WebViewClient.ERROR_HOST_LOOKUP) {
                    onPaymentError(description ?: "Unknown error")
                }
            }
        }
        
        webView.loadUrl(snapUrl)
    }
}

