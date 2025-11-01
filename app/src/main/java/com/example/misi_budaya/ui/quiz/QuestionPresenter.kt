package com.example.misi_budaya.ui.quiz

import com.example.misi_budaya.data.model.Soal
import com.example.misi_budaya.data.repository.QuizRepository

class QuestionPresenter(private val repository: QuizRepository) : QuestionContract.Presenter {

    private var view: QuestionContract.View? = null
    private var soalList: List<Soal> = emptyList()
    private var currentQuestionIndex = 0
    private var userAnswers: MutableMap<String, String> = mutableMapOf()

    override fun onAttach(view: QuestionContract.View) {
        this.view = view
    }

    override fun onDetach() {
        this.view = null
    }

    override fun loadQuestions(paketId: String) {
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
            // Go to the next question
            currentQuestionIndex++
            showCurrentQuestion()
        } else {
            // Finish the quiz and calculate score
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
        view?.navigateToResult(score)
    }
}
