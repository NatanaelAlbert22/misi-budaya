package com.example.misi_budaya.ui.quiz

import com.example.misi_budaya.data.model.Soal
import com.example.misi_budaya.data.repository.QuizRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
        repository.getSoalList(paketId) { result ->
            view?.hideLoading()
            result.onSuccess {
                soalList = it
                if (soalList.isNotEmpty()) {
                    currentQuestionIndex = 0
                    userAnswers.clear()
                    showCurrentQuestion()
                } else {
                    view?.showError("No questions found for this pack.")
                }
            }.onFailure {
                view?.showError(it.message ?: "Failed to load questions.")
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

        // Save the score, and only navigate AFTER the save is complete.
        quizPackName?.let { name ->
            scope.launch {
                repository.updateQuizScore(name, score)
                // Now that the score is saved, navigate back.
                view?.navigateToResult(score)
            }
        }
    }
}
