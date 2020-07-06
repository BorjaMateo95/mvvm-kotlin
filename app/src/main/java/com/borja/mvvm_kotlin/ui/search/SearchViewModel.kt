package com.borja.mvvm_kotlin.ui.search

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.borja.mvvm_kotlin.model.Repo
import com.borja.mvvm_kotlin.repository.RepoRepository
import com.borja.mvvm_kotlin.repository.Resource
import com.borja.mvvm_kotlin.repository.Status
import com.borja.mvvm_kotlin.utils.AbsentLiveData
import java.util.*
import javax.inject.Inject

class SearchViewModel @Inject constructor(repo: RepoRepository) : ViewModel() {

    private val query = MutableLiveData<String>()
    private val nextPageHandler = NextPageHandler(repo)
    val queryLD: LiveData<String> = query

    val result : LiveData<Resource<List<Repo>>> = Transformations.switchMap(query) { search->
        if (search.isNullOrBlank()) {
            AbsentLiveData.create()
        } else {
            repo.search(search)
        }
    }

    val loadMoreStatus: LiveData<LoadMoreState>
        get() = nextPageHandler.loadMoreState

    fun setQuery(originalInput: String) {
        val input = originalInput.toLowerCase(Locale.getDefault()).trim()

        if (input == query.value) {
            return
        }

        nextPageHandler.reset()
        query.value = input
    }

    fun loadNextPage() {
        query.value?.let{
            if (it.isNotBlank()) {
                nextPageHandler.queryNextPage(it)
            }
        }
    }

    fun refresh() {
        query.value?.let {
            query.value = it
        }
    }

    class LoadMoreState(val isRunning: Boolean, val errorMessage: String?) {
        private var handleError = false

        val errorMessageIfNotHandled: String?
        get() {
            if (handleError) {
                return null
            }
            handleError = true
            return errorMessage
        }
    }

    class NextPageHandler(private val repository: RepoRepository): Observer<Resource<Boolean>> {
        private var nextPageLiveData: LiveData<Resource<Boolean>>? = null

        val loadMoreState = MutableLiveData<LoadMoreState>()

        private var query: String? = null
        private var _hasMore: Boolean = false
        val hasMore get() = _hasMore

        init {
            reset()
        }

        fun queryNextPage(query: String) {
            if (this.query == query) {
                return
            }

            unRegister()
            this.query = query
            nextPageLiveData = repository.searchNextPage(query)
            loadMoreState.value = LoadMoreState(isRunning = true, errorMessage = null)
            nextPageLiveData?.observeForever(this)
        }

        override fun onChanged(t: Resource<Boolean>?) {
            if (t == null){
                reset()
            } else {
                when (t.status) {
                    Status.SUCCESS -> {
                        _hasMore = t.data == true
                        unRegister()
                        loadMoreState.setValue(
                            LoadMoreState(
                                isRunning = false,
                                errorMessage = null
                            )
                        )
                    }

                    Status.ERROR -> {
                        _hasMore = true
                        unRegister()
                        loadMoreState.setValue(
                            LoadMoreState(
                                isRunning = false,
                                errorMessage = t.message
                            )
                        )
                    }

                    Status.LOADING -> {

                    }
                }
            }
        }

        fun unRegister() {
            nextPageLiveData?.removeObserver(this)
            nextPageLiveData = null

            if (_hasMore) {
                query = null
            }
        }

        fun reset() {
            unRegister()
            _hasMore = true
            loadMoreState.value = LoadMoreState(
                isRunning = false,
                errorMessage = null
            )
        }
    }
}