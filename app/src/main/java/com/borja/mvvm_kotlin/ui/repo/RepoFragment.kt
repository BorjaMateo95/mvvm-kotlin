package com.borja.mvvm_kotlin.ui.repo

import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.borja.mvvm_kotlin.AppExecutors

import com.borja.mvvm_kotlin.R
import com.borja.mvvm_kotlin.binding.FragmentDataBindingComponent
import com.borja.mvvm_kotlin.databinding.FragmentRepoBinding
import com.borja.mvvm_kotlin.di.Injectable
import com.borja.mvvm_kotlin.ui.common.RetryCallback
import com.borja.mvvm_kotlin.utils.autoCleared
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class RepoFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val repoViewModel: RepoViewModel by viewModels {
        viewModelFactory
    }

    @Inject
    lateinit var appExecutors: AppExecutors

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    var binding by autoCleared<FragmentRepoBinding>()

    private val params by navArgs<RepoFragmentArgs>()
    private var adapter by autoCleared<ContributorAdapter>()
    private fun initContributorList(viewModel: RepoViewModel) {
        viewModel.contribuitors.observe(viewLifecycleOwner, Observer {
            list->
            if (list?.data != null) {
                adapter.submitList(list.data)
            }else{
                adapter.submitList(emptyList())
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var dataBinding = DataBindingUtil.inflate<FragmentRepoBinding>(
            inflater,
            R.layout.fragment_repo,
            container,
            false
        )

        dataBinding.retryCallback = object : RetryCallback {
            override fun retry() {
                repoViewModel.retry()
            }
        }

        binding = dataBinding
//        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(R.transition.move)

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val params = RepoFragmentArgs.fromBundle(arguments!!)
        repoViewModel.setId(params.owner, params.name)
        binding.setLifecycleOwner(viewLifecycleOwner)
        binding.repo = repoViewModel.repo

        val adapter = ContributorAdapter(dataBindingComponent, appExecutors) {contributor->
            findNavController().navigate(
                RepoFragmentDirections.actionRepoFragmentToUserFragment(contributor.avatarUrl, contributor.login)
            )

        }

        this.adapter = adapter
        binding.contributorList.adapter = adapter
        postponeEnterTransition()
        /*binding.contributorList.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }

         */

        initContributorList(repoViewModel)

    }

}
