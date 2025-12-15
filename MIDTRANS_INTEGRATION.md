## Integrasi Midtrans Payment untuk Upgrade Premium

### Alur Kerja:
1. User klik "👑 Upgrade ke Premium" di ProfileScreen
2. Dialog PremiumUpgradeDialog muncul
3. User klik "Mulai Premium Sekarang"
4. App navigate ke PaymentScreen dengan route:
   ```
   payment/{userId}/{userName}/{userEmail}/4999900/Upgrade Premium
   ```
5. PaymentScreen generate Snap token dari Midtrans API
6. Token & transaction dicatat di Firestore
7. WebView menampilkan payment interface Midtrans
8. User memilih metode pembayaran dan lakukan transaksi
9. Setelah pembayaran berhasil:
   - Update transaction status ke "success"
   - Upgrade user ke premium di Firestore (isPremium = true)
   - Navigate kembali ke ProfileScreen

### File yang Ditambah/Diubah:

#### 1. **MidtransService.kt** (`service/`)
- Generate Snap token via REST API
- Setup WebView untuk payment interface
- Build request JSON untuk Midtrans

#### 2. **PaymentScreen.kt** (`ui/payment/`)
- Tampilkan summary pembayaran
- Generate token otomatis saat load
- Embed Midtrans Snap di WebView
- Handle success/error callback

#### 3. **Transaction.kt** (`data/model/`)
- Entity Transaction untuk database
- TransactionRequest untuk API request
- MidtransResponse untuk response

#### 4. **TransactionDao.kt** (`data/model/`)
- CRUD operations untuk Transaction

#### 5. **PaymentRepository.kt** (`data/repository/`)
- recordTransaction() - Catat transaksi ke Firestore
- updateTransactionStatus() - Update status setelah payment
- upgradeToPremiumAfterPayment() - Upgrade user premium
- getTransactionByOrderId() - Query transaksi

#### 6. **ProfileScreen.kt** (`ui/profile/`)
- Modifikasi PremiumUpgradeDialog callback
- Navigate ke PaymentScreen saat user klik upgrade
- Pass user data ke payment screen

#### 7. **MainActivity.kt** (``)
- Tambah Payment route di NavHost
- Parse arguments dari navigation URL

### Konfigurasi yang Diperlukan:

**Di MidtransService.kt, update:**
```kotlin
private const val CLIENT_KEY = "SB-Mid-client-u-78PnYgZV5vKfLX" // Ganti dengan key Anda
```

### Harga Premium:
- **Rp 49.999** per bulan
- Amount dikirim dalam bentuk sen (4999900 = Rp 49.999)

### Metode Pembayaran Tersedia:
- 💳 Kartu Kredit
- 🏦 Transfer Bank
- 📱 E-wallet (GCash, OVO, Dana, dll)
- 🛒 Convenience Store

### Testing:
Gunakan sandbox credentials dari Midtrans dashboard untuk testing.

### Next Steps:
1. Setup webhook Midtrans untuk auto-update status dari server
2. Implement payment success notification
3. Add retry mechanism untuk failed transactions
4. Implement subscription/renewal system jika diperlukan
