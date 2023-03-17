package com.knightboost.optimize.cpuboost

import android.content.Context


interface CpuPerformance {

    /**
     * init method
     * @return <0 meaning init failed.
     */
    fun init(context:Context):Boolean

    /**
     * 加速CPU
     * @param duration 加速持续多久，时间单位为 ms
     */
    fun boostCpu(duration:Int):Boolean

    /**
     * 加速CPU
     * @param duration 加速持续多久，时间单位为 ms
     */
    fun boostCpu(duration:Int,commands:IntArray):Boolean

    /**
     * 取消当前加速效果
     */
    fun stopBoost()

}