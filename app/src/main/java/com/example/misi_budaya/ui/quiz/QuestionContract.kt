package com.example.misi_budaya.ui.quiz

import com.example.misi_budaya.data.model.Question

interface QuestionContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showQuestions(questionList: List<Question>)
        fun showError(message: String)
        fun navigateToResult(score: Int)
        fun showCurrentQuestion(question: Question, number: Int, total: Int)
    }

    interface Presenter {
        fun onAttach(view: View)
        fun onDetach()
        fun loadQuestions(paketId: String)
        fun onAnswerSelected(answerId: String)
        fun onNextOrFinishClicked()
    }
}
