package com.borja.mvvm_kotlin.utils

import androidx.lifecycle.LiveData
import com.borja.mvvm_kotlin.api.ApiResponse
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class LiveDataCallAdapterFactory: CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (CallAdapter.Factory.getRawType(returnType) != LiveData::class.java) {
            return null
        }

        val observable = CallAdapter.Factory.getParameterUpperBound(0, returnType as ParameterizedType)
        val rawObservableType = CallAdapter.Factory.getRawType(observable)

        if (rawObservableType != ApiResponse::class.java) {
            throw IllegalArgumentException("Tipo incorrecto")
        }

        if(observable !is ParameterizedType) {
            throw IllegalArgumentException("Resource must be parameterized")
        }

        val bodyType = CallAdapter.Factory.getParameterUpperBound(0, observable)
        return LiveDataCallAdapter<Any>(bodyType)
    }
}