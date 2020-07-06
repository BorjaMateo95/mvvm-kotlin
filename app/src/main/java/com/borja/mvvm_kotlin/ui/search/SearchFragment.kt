package com.borja.mvvm_kotlin.ui.search

import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.borja.mvvm_kotlin.AppExecutors
import com.borja.mvvm_kotlin.R
import com.borja.mvvm_kotlin.binding.FragmentDataBindingComponent
import com.borja.mvvm_kotlin.databinding.FragmentSearchBinding
import com.borja.mvvm_kotlin.databinding.FragmentSearchBindingImpl
import com.borja.mvvm_kotlin.di.Injectable
import com.borja.mvvm_kotlin.ui.common.RepoListAdapter
import com.borja.mvvm_kotlin.ui.common.RetryCallback
import com.borja.mvvm_kotlin.utils.autoCleared
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class SearchFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    var binding by autoCleared<FragmentSearchBinding>()

    var adapter by autoCleared<RepoListAdapter>()

    val searchViewModel: SearchViewModel by viewModels{
        viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_search,
            container,
            false,
            dataBindingComponent)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.setLifecycleOwner (viewLifecycleOwner)
        initRecyclerView()
        val rvAdapter = RepoListAdapter(
            dataBindingComponent = dataBindingComponent,
            appExecutors = appExecutors,
            showFullName = true
        ){
                repo->
            findNavController().navigate(
                SearchFragmentDirections.actionSearchFragmentToRepoFragment(repo.name, repo.owner.login)
            )
        }
        binding.query = searchViewModel.queryLD
        binding.repoList.adapter = rvAdapter
        adapter = rvAdapter

        initSearchInputListener()

        binding.callback = object: RetryCallback{
            override fun retry() {
                searchViewModel.refresh()
            }
        }
    }

    private fun initSearchInputListener(){
        binding.input.setOnEditorActionListener{view: View, actionId: Int, _: KeyEvent?->
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                doSearch(view)
                true
            } else {
                false
            }
        }

        binding.input.setOnKeyListener{view: View, keyCode: Int, event: KeyEvent->
            if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                doSearch(view)
                true
            }else{
                false
            }
        }
    }

    private fun doSearch(v: View){
        val query = binding.input.text.toString()

        dismissKeyboard(v.windowToken)
        searchViewModel.setQuery(query)
    }

    private fun dismissKeyboard(windowToken: IBinder){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun initRecyclerView(){
        binding.repoList.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastPosition = layoutManager.findLastVisibleItemPosition()
                if(lastPosition == adapter.itemCount - 1){
                    searchViewModel.loadNextPage()
                }
            }
        })

        binding.searchResult = searchViewModel.result
        searchViewModel.result.observe(viewLifecycleOwner, Observer{
                result->
            adapter.submitList(result?.data)
        })

        searchViewModel.loadMoreStatus.observe(viewLifecycleOwner, Observer {
                loadinMore->
            if(loadinMore == null){
                binding.loadingMore = false
            } else {
                binding.loadingMore = loadinMore.isRunning
                val error = loadinMore.errorMessageIfNotHandled
                if(error != null){
                    Log.d("TAG1", "Error")
                }
            }
        })
    }


}
