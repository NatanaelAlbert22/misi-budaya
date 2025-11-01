package com.example.misi_budaya.ui.quiz

import com.example.misi_budaya.data.model.Paket
import com.example.misi_budaya.data.repository.QuizRepository

class QuizPresenter(private val repository: QuizRepository) : QuizContract.Presenter {

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
        repository.getPaketList { result ->
            view?.hideLoading()
            result.onSuccess {
                // Filter out secret packs if necessary, or handle as needed
                val visiblePacks = it.filter { !it.isSecret }
                view?.showQuizPacks(visiblePacks)
            }.onFailure {
                view?.showError(it.message ?: "Failed to load quiz packs.")
            }
        }
    }

    override fun onPaketClicked(paket: Paket) {
        // Perbaikan: Gunakan namaPaket untuk query, bukan ID dokumen
        view?.navigateToQuestions(paket.namaPaket)
    }
}
