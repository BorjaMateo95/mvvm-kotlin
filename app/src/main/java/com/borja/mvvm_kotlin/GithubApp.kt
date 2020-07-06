package com.borja.mvvm_kotlin

import android.app.Activity
import android.app.Application
import com.borja.mvvm_kotlin.di.AppInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class GithubApp : Application(), HasActivityInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
    }


    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingAndroidInjector
    }

}