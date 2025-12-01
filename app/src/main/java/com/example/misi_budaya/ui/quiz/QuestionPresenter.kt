package com.example.misi_budaya.ui.quiz

import com.example.misi_budaya.data.model.Question
import com.example.misi_budaya.data.repository.QuizRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class QuestionPresenter(private val repository: QuizRepository, private val scope: CoroutineScope) : QuestionContract.Presenter {

    private var view: QuestionContract.View? = null
    private var questionList: List<Question> = emptyList()
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
        scope.launch {
            try {
                val questions = repository.getSoalList(paketId)
                view?.hideLoading()
                if (questions.isNotEmpty()) {
                    questionList = questions
                    currentQuestionIndex = 0
                    userAnswers.clear()
                    showCurrentQuestion()
                } else {
                    view?.showError("No questions found for this pack.")
                }
            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError(e.message ?: "Failed to load questions.")
            }
        }
    }

    override fun onAnswerSelected(answerId: String) {
        if (questionList.isNotEmpty()) {
            val currentQuestionId = questionList[currentQuestionIndex].id
            userAnswers[currentQuestionId] = answerId
        }
    }

    override fun onNextOrFinishClicked() {
        if (currentQuestionIndex < questionList.size - 1) {
            currentQuestionIndex++
            showCurrentQuestion()
        } else {
            finishQuiz()
        }
    }

    private fun showCurrentQuestion() {
        val currentQuestion = questionList[currentQuestionIndex]
        view?.showCurrentQuestion(currentQuestion, currentQuestionIndex + 1, questionList.size)
    }

    private fun finishQuiz() {
        var correctAnswers = 0
        questionList.forEach { question ->
            if (userAnswers[question.id] == question.correctAnswerId) {
                correctAnswers++
            }
        }
        val score = if (questionList.isNotEmpty()) (correctAnswers * 100) / questionList.size else 0

        quizPackName?.let { name ->
            scope.launch {
                repository.updateQuizScore(name, score)
                view?.navigateToResult(score)
            }
        }
    }
}
