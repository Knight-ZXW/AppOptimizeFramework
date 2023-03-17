package com.knightboost.optimize.cpuboost

import android.content.Context

class VoidCpuPerformance :CpuPerformance{
    override fun init(context: Context): Boolean {
        return false
    }

    override fun boostCpu(duration: Int): Boolean {
        return false
    }

    override fun boostCpu(duration: Int, commands: IntArray): Boolean {
        return false
    }

    override fun stopBoost() {
    }
}