package com.knightboost.optimize.preload

import android.os.SystemClock
import android.util.Log
import androidx.annotation.WorkerThread
import com.knightboost.preload.PreloadDemander

/**
 * 类预加载执行器
 */
object ClassPreloadExecutor {

    var debug = false
    private const val TAG = "ClassPreloadExecutor"

    private val demanders = mutableListOf<PreloadDemander>()

    fun addDemander(classPreloadDemander: PreloadDemander) {
        demanders.add(classPreloadDemander)
    }

    /**
     * this method shouldn't run on main thread
     */
    @WorkerThread fun doPreload() {
        try {
            val localDemanders = demanders.toTypedArray()
            val beginTime = SystemClock.elapsedRealtimeNanos()
            val cpuBeginTime = SystemClock.currentThreadTimeMillis()

            var counter = 0
            for (demander in localDemanders) {
                val classes = demander.preloadClasses
                classes.forEach {
                    val classLoader = ClassPreloadExecutor::class.java.classLoader
                    var iBeginTime = 0L
                    var iCpuBeginTime = 0L
                    if (debug) {
                        iBeginTime = SystemClock.elapsedRealtimeNanos()
                        iCpuBeginTime = SystemClock.currentThreadTimeMillis()
                    }

                    Class.forName(it.name, true, classLoader)

                    counter++
                    if (debug) {
                        val iEndTime = SystemClock.elapsedRealtimeNanos()
                        val iCpuEndTime = SystemClock.currentThreadTimeMillis()
                        Log.d(
                            TAG,
                            "class preload for " + it.name
                                + " cost wallTime = ${(iEndTime - iBeginTime) / 1000} microseconds"
                                + " cost cpuTime= ${iCpuEndTime - iCpuBeginTime} ms"
                        )
                    }
                }
            }
            val endTime = SystemClock.elapsedRealtimeNanos()
            val cpuEndTime = SystemClock.currentThreadTimeMillis()
            if (debug) {
                Log.d(
                    TAG,"preload $counter classes cost wallTime: ${(endTime - beginTime) / 1000_000} ms," + " cpuTime ${cpuEndTime - cpuBeginTime} ms")
            }


        } catch (e: Exception) {
            Log.e(
                TAG, "classPreloadFailed")
        }
    }
}
