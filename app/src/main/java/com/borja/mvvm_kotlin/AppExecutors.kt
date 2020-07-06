package com.borja.mvvm_kotlin

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clase para gestionar los hilos, bd, red o hilo principal
 */
@Singleton
open class AppExecutors(
    val diskIo: Executor,
    val networkIo: Executor,
    val mainThread: Executor
) {

    @Inject
    constructor(): this(
        Executors.newSingleThreadExecutor(),
        Executors.newFixedThreadPool(3),
        MainThreadExecutor()
    )

    fun diskIO(): Executor{
        return diskIo
    }

    fun networkIO(): Executor{
        return networkIo
    }

    fun mainThread(): Executor {
        return mainThread
    }

    private class MainThreadExecutor: Executor{
        val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }


}