package com.example.misi_budaya.ui.quiz

import com.example.misi_budaya.data.model.QuizPackage
import com.example.misi_budaya.data.repository.QuizRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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

        repository.getPaketList()
            .onEach { quizPackages ->
                view?.hideLoading()
                if (quizPackages.isNotEmpty()) {
                    view?.showQuizPacks(quizPackages)
                }
            }
            .catch { e ->
                view?.hideLoading()
                view?.showError(e.message ?: "Failed to load quiz packs from local source.")
            }
            .launchIn(scope)

        scope.launch {
            try {
                repository.refreshPaketList()
            } catch (e: Exception) {
                view?.hideLoading()
                view?.showError(e.message ?: "Failed to refresh quiz packs from remote source.")
            }
        }
    }

    override fun onPaketClicked(paket: QuizPackage) {
        view?.navigateToQuestions(paket.name) // Navigate with the quiz name (the primary key)
    }
}
