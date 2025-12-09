package com.example.misi_budaya.ui.quiz

import android.util.Log
import com.example.misi_budaya.data.model.QuizPackage
import com.example.misi_budaya.data.repository.QuizRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class QuizPresenter(private val repository: QuizRepository, private val scope: CoroutineScope) : QuizContract.Presenter {

    private var view: QuizContract.View? = null

    override fun onAttach(view: QuizContract.View) {
        this.view = view
        loadQuizPacks()
    }

    override fun onDetach() {
        this.view = null
    }

    override fun loadQuizPacks() {
        view?.showLoading()

        // COROUTINE #1: Mengambil data lokal menggunakan Flow (reactive)
        // Flow akan otomatis update UI ketika database berubah
        repository.getPaketList()
            .onEach { quizPackages ->
                view?.hideLoading()
                if (quizPackages.isNotEmpty()) {
                    view?.showQuizPacks(quizPackages)
                }
            }
            .catch { e ->
                Log.e("QuizPresenter", "Error loading local data", e)
                view?.hideLoading()
                view?.showError(e.message ?: "Gagal memuat data lokal.")
            }
            .launchIn(scope) // Launch di scope yang sudah di-bind ke lifecycle

        // COROUTINE #2: Refresh data dari Firebase dengan retry & timeout
        scope.launch {
            try {
                // withContext(Dispatchers.IO) memastikan operasi berjalan di background thread
                withContext(Dispatchers.IO) {
                    // Retry mechanism: Coba 3 kali jika gagal
                    var retryCount = 0
                    val maxRetries = 3
                    var lastException: Exception? = null
                    var success = false

                    while (retryCount < maxRetries && !success) {
                        try {
                            // withTimeout: Batalkan jika lebih dari 30 detik
                            withTimeout(30_000L) {
                                Log.d("QuizPresenter", "Refreshing from Firebase (attempt ${retryCount + 1})")
                                repository.refreshPaketList()
                            }
                            success = true // Sukses, keluar dari retry loop
                        } catch (e: Exception) {
                            lastException = e
                            retryCount++
                            if (retryCount < maxRetries) {
                                Log.w("QuizPresenter", "Retry $retryCount after error: ${e.message}")
                                delay(2000L * retryCount) // Exponential backoff: 2s, 4s, 6s
                            }
                        }
                    }

                    // Jika semua retry gagal, throw exception terakhir
                    if (!success) {
                        throw lastException ?: Exception("Unknown error")
                    }
                }
            } catch (e: Exception) {
                // withContext(Dispatchers.Main) untuk update UI di main thread
                withContext(Dispatchers.Main) {
                    Log.e("QuizPresenter", "Failed to refresh from Firebase after retries", e)
                    view?.hideLoading()
                    // Tidak show error jika data lokal sudah ada
                    // view?.showError(e.message ?: "Gagal refresh dari server.")
                }
            }
        }
    }

    override fun onPaketClicked(paket: QuizPackage) {
        view?.navigateToQuestions(paket.name) // Navigate with the quiz name (the primary key)
    }

    // New: explicit refresh handler for pull-to-refresh
    override fun onRefresh() {
        // Do not show full-screen loading here because pull-to-refresh indicator is used in UI
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    var retryCount = 0
                    val maxRetries = 3
                    var lastException: Exception? = null
                    var success = false

                    while (retryCount < maxRetries && !success) {
                        try {
                            withTimeout(30_000L) {
                                Log.d("QuizPresenter", "Manual refresh from Firebase (attempt ${retryCount + 1})")
                                repository.refreshPaketList()
                            }
                            success = true
                        } catch (e: Exception) {
                            lastException = e
                            retryCount++
                            if (retryCount < maxRetries) {
                                delay(2000L * retryCount)
                            }
                        }
                    }

                    if (!success) {
                        throw lastException ?: Exception("Unknown error")
                    }
                }

                // success -> local Flow from repository will propagate updated data to view
            } catch (e: Exception) {
                Log.e("QuizPresenter", "Manual refresh failed", e)
                // keep silent if local data exists; optionally view?.showError(...)
            } finally {
                withContext(Dispatchers.Main) {
                    view?.hideLoading()
                }
            }
        }
    }
}
