package com.example.misi_budaya.ui.quiz

import com.example.misi_budaya.data.model.Soal

interface QuestionContract {

    interface View {
        fun showLoading()
        fun hideLoading()
        fun showQuestions(soalList: List<Soal>)
        fun showError(message: String)
        fun navigateToResult(score: Int)
        fun showCurrentQuestion(soal: Soal, questionNumber: Int, totalQuestions: Int)
    }

    interface Presenter {
        fun onAttach(view: View)
        fun onDetach()
        fun loadQuestions(paketId: String)
        fun onAnswerSelected(answerId: String)
        fun onNextOrFinishClicked()
    }
}
