package com.knightboost.kprofiler

import android.app.Application
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import dalvik.system.BaseDexClassLoader
import dalvik.system.PathClassLoader

class MonitorClassLoader(
    dexPath: String,
    parent: ClassLoader, private val onlyMainThread: Boolean = false,
) : PathClassLoader(dexPath, parent) {

    val TAG = "MonitorClassLoader"

    companion object {
        @JvmStatic
        fun hook(application: Application, onlyMainThread: Boolean = false) {
            val pathClassLoader = application.classLoader
            try {
                val monitorClassLoader = MonitorClassLoader("", pathClassLoader.parent, onlyMainThread)
                val pathListField = BaseDexClassLoader::class.java.getDeclaredField("pathList")
                pathListField.isAccessible = true
                val pathList = pathListField.get(pathClassLoader)
                pathListField.set(monitorClassLoader, pathList)

                val parentField = ClassLoader::class.java.getDeclaredField("parent")
                parentField.isAccessible = true
                parentField.set(pathClassLoader, monitorClassLoader)
            } catch (throwable: Throwable) {
                Log.e("hook", throwable.stackTraceToString())
            }
        }
        var printClassEnable = false

    }

    override fun loadClass(name: String?, resolve: Boolean): Class<*> {
            if (onlyMainThread && Looper.getMainLooper().thread!=Thread.currentThread()){
            return super.loadClass(name, resolve)
        }
        val begin = SystemClock.elapsedRealtimeNanos()
        val clazz = super.loadClass(name, resolve)
        if (!printClassEnable){
            return clazz
        }

        val end = SystemClock.elapsedRealtimeNanos()
        val cost = end - begin
        if (cost > 1000_000){
            Log.e(TAG, "加载 ${clazz} 耗时 ${(end - begin) / 1000} 微秒 ,线程ID ${Thread.currentThread().id}")
        } else {
            Log.d(TAG, "加载 ${clazz} 耗时 ${(end - begin) / 1000} 微秒 ,线程ID ${Thread.currentThread().id}")
        }
        return  clazz

    }
}