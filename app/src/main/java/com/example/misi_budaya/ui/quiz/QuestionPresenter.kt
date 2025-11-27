package com.example.misi_budaya.ui.quiz

import android.util.Log
import com.example.misi_budaya.data.model.Soal
import com.example.misi_budaya.data.repository.QuizRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuestionPresenter(private val repository: QuizRepository, private val scope: CoroutineScope) : QuestionContract.Presenter {

    private var view: QuestionContract.View? = null
    private var soalList: List<Soal> = emptyList()
    private var currentQuestionIndex = 0
    private var userAnswers: MutableMap<String, String> = mutableMapOf()
    private var quizPackName: String? = null // Store the name of the quiz pack

    override fun onAttach(view: QuestionContract.View) {
        this.view = view
    }

    override fun onDetach() {
        this.view = null
    }

    override fun loadQuestions(paketId: String) {
        this.quizPackName = paketId // Save the name for later
        view?.showLoading()
        
        // COROUTINE: Launch di scope yang di-bind ke lifecycle
        scope.launch {
            try {
                // getSoalList sekarang suspend function, jadi bisa langsung di-call
                // tanpa callback. Lebih clean dan mudah di-maintain!
                val questions = repository.getSoalList(paketId)
                
                // withContext(Dispatchers.Main) untuk update UI di main thread
                withContext(Dispatchers.Main) {
                    view?.hideLoading()
                    
                    if (questions.isNotEmpty()) {
                        soalList = questions
                        currentQuestionIndex = 0
                        userAnswers.clear()
                        showCurrentQuestion()
                        Log.d("QuestionPresenter", "Loaded ${questions.size} questions")
                    } else {
                        view?.showError("Tidak ada soal untuk paket ini.")
                        Log.w("QuestionPresenter", "No questions found for paketId: $paketId")
                    }
                }
            } catch (e: Exception) {
                // Error handling dengan coroutine lebih mudah dengan try-catch
                withContext(Dispatchers.Main) {
                    view?.hideLoading()
                    view?.showError(e.message ?: "Gagal memuat soal.")
                    Log.e("QuestionPresenter", "Error loading questions", e)
                }
            }
        }
    }

    override fun onAnswerSelected(answerId: String) {
        if (soalList.isNotEmpty()) {
            val currentSoalId = soalList[currentQuestionIndex].id
            userAnswers[currentSoalId] = answerId
        }
    }

    override fun onNextOrFinishClicked() {
        if (currentQuestionIndex < soalList.size - 1) {
            currentQuestionIndex++
            showCurrentQuestion()
        } else {
            finishQuiz()
        }
    }

    private fun showCurrentQuestion() {
        val currentSoal = soalList[currentQuestionIndex]
        view?.showCurrentQuestion(currentSoal, currentQuestionIndex + 1, soalList.size)
    }

    private fun finishQuiz() {
        var correctAnswers = 0
        soalList.forEach { soal ->
            if (userAnswers[soal.id] == soal.jawabanBenar) {
                correctAnswers++
            }
        }
        val score = if (soalList.isNotEmpty()) (correctAnswers * 100) / soalList.size else 0

        // COROUTINE: Save score di background, lalu navigate di main thread
        quizPackName?.let { name ->
            scope.launch {
                try {
                    // updateQuizScore sudah menggunakan withContext(Dispatchers.IO)
                    repository.updateQuizScore(name, score)
                    Log.d("QuestionPresenter", "Score saved: $score for $name")
                    
                    // Navigate setelah score berhasil disimpan
                    withContext(Dispatchers.Main) {
                        view?.navigateToResult(score)
                    }
                } catch (e: Exception) {
                    Log.e("QuestionPresenter", "Error saving score", e)
                    // Tetap navigate meskipun gagal save score
                    withContext(Dispatchers.Main) {
                        view?.navigateToResult(score)
                    }
                }
            }
        }
    }
}
