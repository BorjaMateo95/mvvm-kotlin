package com.borja.mvvm_kotlin.di

import android.app.Application
import com.borja.mvvm_kotlin.GithubApp
import com.borja.mvvm_kotlin.MainActivity
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    MainActivityModule::class
    ]
)
interface AppComponent {

    @Component.Builder
    interface Builder{
        @BindsInstance
        fun application(application: Application): Builder
        fun build() : AppComponent
    }


    fun inject(githubApp: GithubApp)
}