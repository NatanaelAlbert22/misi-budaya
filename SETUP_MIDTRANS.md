# 🔧 SETUP MIDTRANS - WAJIB DILAKUKAN!

Error "Failed to generate Snap token" terjadi karena **CLIENT_KEY belum dikonfigurasi dengan benar**.

## ✅ Langkah-Langkah Setup:

### 1. Daftar Midtrans Account
- Buka https://dashboard.sandbox.midtrans.com
- Klik "Sign Up"
- Isi email dan password
- Verifikasi email Anda

### 2. Dapatkan Client Key
- Login ke dashboard
- Klik menu **Settings** (ikon gear)
- Pilih **Access Keys** di sidebar kiri
- Copy **Client Key** (bukan Server Key)
- Contoh: `SB-Mid-client-xxxxxxxxxxxxx`

### 3. Update Client Key di Project
Buka file: `app/src/main/java/com/example/misi_budaya/config/MidtransConfig.kt`

Cari baris ini:
```kotlin
const val CLIENT_KEY = "SB-Mid-client-u-78PnYgZV5vKfLX"
```

Ganti dengan Client Key Anda:
```kotlin
const val CLIENT_KEY = "SB-Mid-client-YOUR_ACTUAL_KEY_HERE"
```

### 4. Sync & Rebuild Project
```bash
# Di terminal project root
./gradlew clean build
```

Atau gunakan Android Studio:
- File → Sync Now
- Build → Rebuild Project

### 5. Test
- Run app
- Buka Profile → Klik "Upgrade Premium"
- Seharusnya sekarang bisa generate payment token

---

## 🎯 Untuk Production (Live)

Ketika siap production:

1. **Ganti credentials** di `MidtransConfig.kt`:
```kotlin
const val ENVIRONMENT = "production"  // Ubah dari "sandbox"
const val CLIENT_KEY = "Mid-client-xxx"  // Pakai production key
```

2. **Dapatkan Production Key** dari Midtrans Dashboard:
   - Settings → Access Keys
   - Tab "Production"

---

## 🐛 Debugging Tips

Jika masih error, cek:

1. **Check logcat di Android Studio:**
   - Buka Logcat
   - Filter: `MidtransService`
   - Lihat detailed error messages

2. **Verify internet connection:**
   - Pastikan device/emulator terhubung internet
   - Test di emulator dengan "Use Host GPU" disabled jika ada masalah

3. **Verify CLIENT_KEY:**
   - Pastikan tidak ada space atau typo
   - Pastikan copy dari dashboard, bukan dari dokumentasi

4. **Clear app data:**
   - Settings → Apps → Misi Budaya → Storage → Clear Data
   - Re-run app

---

## 📋 Checklist Setup

- [ ] Daftar Midtrans Sandbox account
- [ ] Copy Client Key dari dashboard
- [ ] Update CLIENT_KEY di MidtransConfig.kt
- [ ] Sync & rebuild project
- [ ] Test upgrade premium
- [ ] Verify payment interface muncul

---

## 💡 Testing dengan Midtrans Snap

Setelah token berhasil generate, Anda akan melihat payment interface dengan pilihan:
- 💳 Kartu Kredit
- 🏦 Transfer Bank
- 📱 E-wallet
- 🛒 Convenience Store

Gunakan test credentials yang ada di dokumentasi Midtrans untuk testing pembayaran.

---

**Masih error? Check logcat untuk error details!** 🔍
