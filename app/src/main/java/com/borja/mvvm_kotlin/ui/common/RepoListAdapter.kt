package com.borja.mvvm_kotlin.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.borja.mvvm_kotlin.AppExecutors
import com.borja.mvvm_kotlin.R
import com.borja.mvvm_kotlin.databinding.RepoItemBinding
import com.borja.mvvm_kotlin.model.Repo

class RepoListAdapter(
    private val dataBindingComponent: DataBindingComponent,
    appExecutors: AppExecutors,
    private val showFullName: Boolean,
    private val repoClickCallback: ((Repo)->Unit)?
) : DataBoundListAdapter<Repo, RepoItemBinding>(
    appExecutors = appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Repo>() {
        override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
            return oldItem.owner == newItem.owner && oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
            return oldItem.description == newItem.description && oldItem.stars == newItem.stars
        }

    }
) {
    override fun createBinding(parent: ViewGroup): RepoItemBinding {
        val binding = DataBindingUtil.inflate<RepoItemBinding>(
            LayoutInflater.from(parent.context), R.layout.repo_item,
            parent,
            false,
            dataBindingComponent
        )

        binding.showFullName = showFullName
        binding.root.setOnClickListener{
            binding.repo?.let {
                repoClickCallback?.invoke(it)
            }
        }

        return binding
    }

    override fun bind(binding: RepoItemBinding, item: Repo) {
        binding.repo = item
    }


}