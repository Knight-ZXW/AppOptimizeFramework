package com.knightboost.optimize.cpuboost

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import com.knightboost.artvm.ArtThread
import timber.log.Timber
import android.os.Process

@SuppressLint("StaticFieldLeak")
object CpuBoostManager {

    private var printErrorLog = true

    private var inited = false;

    private lateinit var context: Context

    const val TAG = "CpuBoostManager"

    private var cpuBoostEnable = false;

    private lateinit var cpuPerformance: CpuPerformance


    /**
     * @param context:Context 请使用Application 对应的Context
     */
    @Synchronized
    fun init(context: Context) {
        if (inited){
            return
        }
        this.context = context
        if (context is Activity) {
            Timber.tag(TAG).e("please use application context instead of activity ")
        }

        val hardware = Build.HARDWARE
        //TODO 测试并适配更多hardware
        if (hardware.equals("qcom")) {
            cpuPerformance  =QcmCpuPerformance()
            cpuBoostEnable = cpuPerformance.init(context)
        } else { //暂未适配其他CPU型号
            cpuPerformance = VoidCpuPerformance()
            cpuBoostEnable = false
        }
        inited = true
    }

    fun boostCpu(duration: Int, reason: String=""): Boolean {
        if (!inited){
            Timber.tag(TAG).w("you should call `boostCpu` method after `init` method");
            return false
        }
        return cpuPerformance.boostCpu(duration)
    }


    /**
     * 提升线程优先级
     */
    fun setThreadPriority(thread: Thread, priority: Int) {
        if (!thread.isAlive) {
            Log.e(TAG, "目标线程${thread.name}未启动或存活，提升线程优先级失败");
            return
        }
        Process.setThreadPriority(ArtThread.getTid(thread), priority)
    }

    /**
     * 提升线程优先级
     */
    fun setThreadPriority(tid: Int, priority: Int) {
        Process.setThreadPriority(tid, priority)
    }


    /**
     * 通常你不需要主动调用该函数，CPU的加速在指定的时间后会自动停止。
     * 注意 如果调用了stopBoost 会将所有 CPU加速功能停止。
     */
    fun stopBoost() {
        if (inited){
            return
        }
        return cpuPerformance.stopBoost()
    }


    fun boostErrorLog(tag: String, msg: String,
       exception: java.lang.Exception?) {
        if (!printErrorLog) {
            return
        }
        Timber.tag(tag).e(exception, msg)
    }

//    /**
//     * io资源预读 开始
//     */
//    fun startIoPrefetch(codePath:String){
//        if (cpuPerformance is QcmCpuPerformance){
//            (cpuPerformance as QcmCpuPerformance).perfIOPrefetchStart(context.packageName,codePath)
//        }
//    }
//
//    fun stopIoPrefetch(){
//        if (cpuPerformance is QcmCpuPerformance){
//            (cpuPerformance as QcmCpuPerformance).stopBoost()
//        }
//    }

}