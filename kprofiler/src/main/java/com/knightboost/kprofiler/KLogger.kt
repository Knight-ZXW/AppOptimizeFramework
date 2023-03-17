package com.knightboost.kprofiler

import android.util.Log

class KLogger {
    companion object {
        const val TAG = "KProfiler"
        @JvmStatic fun e(tag: String, msg: String) {
            Log.e(tag, msg)
        }
        @JvmStatic fun d(tag: String, msg: String) {
            Log.d(tag, msg)
        }
        @JvmStatic fun w(tag: String, msg: String) {
            Log.w(tag, msg)
        }
    }

}