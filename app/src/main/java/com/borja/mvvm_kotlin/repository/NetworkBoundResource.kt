package com.borja.mvvm_kotlin.repository

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.borja.mvvm_kotlin.AppExecutors
import com.borja.mvvm_kotlin.api.ApiEmtyResponse
import com.borja.mvvm_kotlin.api.ApiErrorResponse
import com.borja.mvvm_kotlin.api.ApiResponse
import com.borja.mvvm_kotlin.api.ApiSuccessResponse

abstract class NetworkBoundResource<ResultType, RequestType>

//etiqueta para que se llame siempre desde el hilo principal
@MainThread constructor(private val appExecutors: AppExecutors) {
    //MediatorLiveData mergea todos los liveData en uno y solo tenemos que mirar este
    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        result.value = Resource.loading(null)
        val dbSource = loadFromDB()
        result.addSource(dbSource) { data ->
            result.removeSource(dbSource)
            if (shouldFetch(data)) {
                fetchFromNetwork(dbSource)
            } else {
                result.addSource(dbSource) {newData->
                    setValue(Resource.success(newData))

                }
            }
        }
    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>) {
        if (result.value != newValue) {
            result.value = newValue
        }
    }

    /*
        - Carga los datos de local mientras hace la peticion y lo refresca
     */
    private fun fetchFromNetwork(dbSource: LiveData<ResultType>) {
        val apiResponse: LiveData<ApiResponse<RequestType>> = createCall()
        result.addSource(dbSource) {newData->
            setValue(Resource.loading(newData))
        }

        result.addSource(apiResponse) {response ->
            result.removeSource(apiResponse)
            result.removeSource(dbSource)
            when(response) {
                // cuando llegan los datos se guardan en local
                is ApiSuccessResponse-> {
                    appExecutors.diskIO().execute{
                        saveCallResult(processResponse(response))
                        appExecutors.mainThread().execute {
                            //siempre leemos de la db local una unica fuente de datos
                            result.addSource(loadFromDB()) {newData->
                                setValue(Resource.success(newData))
                            }
                        }
                    }
                }
                is ApiEmtyResponse->{
                    appExecutors.mainThread().execute {
                        result.addSource(loadFromDB()) {newData->
                            setValue(Resource.success(newData))

                        }
                    }
                }

                is ApiErrorResponse->{
                    onFetchFailed()
                    result.addSource(dbSource) {newData->
                        setValue(Resource.error((response as ApiErrorResponse).errorMessage, newData))
                    }
                }
            }

        }
    }

    protected open fun onFetchFailed(){}

    fun asLiveData() = result as LiveData<Resource<ResultType>>

    @WorkerThread
    protected open fun processResponse(response: ApiSuccessResponse<RequestType>) = response.body

    @WorkerThread
    protected abstract fun saveCallResult(item: RequestType)

    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean

    @MainThread
    protected abstract fun loadFromDB(): LiveData<ResultType>

    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<RequestType>>
}