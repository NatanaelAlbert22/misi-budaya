package com.example.misi_budaya.ui.quiz

import com.example.misi_budaya.data.model.QuizPackage

interface QuizContract {

    interface View {
        fun showLoading()
        fun hideLoading()
        fun showQuizPacks(paketList: List<QuizPackage>)
        fun showError(message: String)
        fun navigateToQuestions(paketId: String)
    }

    interface Presenter {
        fun onAttach(view: View)
        fun onDetach()
        fun loadQuizPacks()
        fun onPaketClicked(paket: QuizPackage)
    }
}
