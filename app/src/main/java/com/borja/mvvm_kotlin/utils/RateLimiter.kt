package com.borja.mvvm_kotlin.utils

import android.os.SystemClock
import android.util.ArrayMap
import java.util.concurrent.TimeUnit

class RateLimiter<in KEY>(timeout: Int, timeUnit: TimeUnit){
    private val timestamps = ArrayMap<KEY, Long>()
    private val timeout = timeUnit.toMillis(timeout.toLong())

    @Synchronized
    fun shoulFetch(key: KEY): Boolean {
        val lastFeched: Long? = timestamps[key]
        val now: Long = now()

        if (lastFeched == null) {
            timestamps[key] = now
            return true
        }

        if (now - lastFeched > timeout) {
            timestamps[key] = now
            return true
        }

        return false
    }

    private fun now() = SystemClock.uptimeMillis()

    @Synchronized
    fun reset(key: KEY){
        timestamps.remove(key)
    }
}