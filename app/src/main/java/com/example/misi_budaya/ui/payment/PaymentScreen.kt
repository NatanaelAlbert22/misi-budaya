package com.example.misi_budaya.ui.payment

import android.widget.Toast
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.misi_budaya.data.model.TransactionRequest
import com.example.misi_budaya.data.repository.PaymentRepository
import com.example.misi_budaya.service.MidtransService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    userId: String,
    userName: String,
    userEmail: String,
    amount: Long,
    description: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val midtransService = remember { MidtransService(context) }
    val paymentRepository = remember { PaymentRepository() }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var snapToken by remember { mutableStateOf<String?>(null) }
    var snapUrl by remember { mutableStateOf<String?>(null) }
    var orderId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var showWebView by remember { mutableStateOf(false) }
    var transactionId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // Generate token saat screen pertama kali dibuka
        isLoading = true
        scope.launch {
            val transactionRequest = TransactionRequest(
                userId = userId,
                amount = amount,
                description = description,
                email = userEmail,
                firstName = userName
            )
            
            val result = midtransService.generateSnapToken(transactionRequest)
            result.onSuccess { response ->
                snapToken = response.token
                snapUrl = response.redirectUrl
                orderId = response.orderId
                isLoading = false
                
                // Record transaction ke database
                scope.launch {
                    paymentRepository.recordTransaction(
                        userId = userId,
                        orderId = response.orderId,
                        amount = amount,
                        description = description,
                        status = "pending"
                    ).onSuccess { txnId ->
                        transactionId = txnId
                    }
                }
            }.onFailure { exception ->
                error = exception.message ?: "Failed to generate payment token"
                isLoading = false
                onError(error ?: "Unknown error")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pembayaran") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1F2937)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(paddingValues)
        ) {
            if (isLoading && !showWebView) {
                // Loading state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Mempersiapkan pembayaran...")
                }
            } else if (showWebView && snapUrl != null) {
                // WebView dengan Midtrans Snap
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            midtransService.setupWebViewForPayment(
                                this,
                                snapUrl!!,
                                onPaymentSuccess = { token ->
                                    onSuccess(token)
                                },
                                onPaymentError = { errorMsg ->
                                    error = errorMsg
                                    onError(errorMsg)
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (error != null) {
                // Error state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .wrapContentSize(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error ?: "Terjadi kesalahan",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack) {
                        Text("Kembali")
                    }
                }
            } else {
                // Payment summary
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Payment Summary Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Ringkasan Pembayaran",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            
                            // User info
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Nama:", color = Color.Gray)
                                Text(userName, fontWeight = FontWeight.Bold)
                            }
                            
                            // Email
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Email:", color = Color.Gray)
                                Text(userEmail, fontWeight = FontWeight.Bold)
                            }
                            
                            // Description
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Deskripsi:", color = Color.Gray)
                                Text(description, fontWeight = FontWeight.Bold)
                            }
                            
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            
                            // Amount
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Pembayaran:", fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Rp ${String.format("%,d", amount / 100)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF059669)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Payment method info
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Metode Pembayaran",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            
                            Text(
                                text = "Anda akan diarahkan ke gateway pembayaran Midtrans untuk melakukan transaksi secara aman.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Metode pembayaran yang tersedia:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Payment methods list
                            listOf(
                                "💳 Kartu Kredit",
                                "🏦 Transfer Bank",
                                "📱 E-wallet (GCash, OVO, Dana, dll)",
                                "🛒 Convenience Store"
                            ).forEach { method ->
                                Text(
                                    text = "• $method",
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Proceed button
                    Button(
                        onClick = {
                            if (snapUrl != null) {
                                showWebView = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1F2937)
                        ),
                        enabled = snapUrl != null
                    ) {
                        Text(
                            text = "Lanjutkan ke Pembayaran",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cancel button
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Batal")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

