package com.borja.mvvm_kotlin.di

import com.borja.mvvm_kotlin.ui.repo.RepoFragment
import com.borja.mvvm_kotlin.ui.search.SearchFragment
import com.borja.mvvm_kotlin.ui.user.UserFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeRepoFragment(): RepoFragment

    @ContributesAndroidInjector
    abstract fun contributeUserFragment(): UserFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment

}