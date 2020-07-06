package com.borja.mvvm_kotlin.utils

import androidx.lifecycle.LiveData

class AbsentLiveData<T: Any?> private constructor() : LiveData<T>() {
    init {
        postValue(null)
    }

    companion object {
        fun <T> create(): LiveData<T>{
            return AbsentLiveData()
        }
    }
}