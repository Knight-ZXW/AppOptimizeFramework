package com.knightboost.appoptimizeframework.cpu

import android.util.Log
import java.io.File
import java.io.FilenameFilter
import java.util.regex.Pattern

object CpuReadUtil {
    val cpuFiles by lazy {
        val cpuFile = File("/sys/devices/system/cpu")
        val filter = object : FilenameFilter {
            override fun accept(dir: File, name: String): Boolean {
                return Pattern.matches("cpu[0-9]", name)
            }
        }
        return@lazy cpuFile.listFiles(filter)?: emptyArray()
    }


    fun printAllCpuFreq(){
        val size = cpuFiles.size
        try {
            var totalCurFreq =0L
            var totalMinFreq =0L
            var totalMaxFreq =0L
            for (i in 0 until  size){
                val scalingMinFreq = scalingMinFreq(i)
                val scalingMaxFreq = scalingMaxFreq(i)
                val scalingCurFreq = scalingCurFreq(i)
                totalMinFreq+=scalingCurFreq
                totalMaxFreq+=scalingMaxFreq
                totalCurFreq +=scalingCurFreq
                Log.w("cpuFreq","cpu ${i} " +
                        "最小频率 ${scalingMinFreq}kHZ, 最大频率 ${scalingMaxFreq}kHZ 当前频率 ${scalingCurFreq}kHZ")
            }
            Log.w("cupFreq", "总计->最小频率 ${totalMinFreq}kHZ, 最大频率 ${totalMaxFreq}kHZ 当前频率 ${totalCurFreq}kHZ")
            Log.w("cpuFreq","=============================")
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    fun readAllCpuFreq(){
        var cpuDir = File("/sys/devices/system/cpu")
    }

    fun scalingMaxFreq(cpuIndex: Int): Long {
        return readLong(
            cpuIndexPath(cpuIndex),
            "cpufreq/scaling_max_freq"
        )
    }

    fun scalingMinFreq(cpuIndex: Int): Long {
        return readLong(
            cpuIndexPath(cpuIndex),
            "cpufreq/scaling_min_freq"
        )
    }

    fun scalingCurFreq(cpuIndex: Int): Long {
        return readLong(
            cpuIndexPath(cpuIndex),
            "cpufreq/scaling_cur_freq"
        )
    }

    private fun cpuIndexPath(cpuIndex: Int): String {
        return "/sys/devices/system/cpu/cpu$cpuIndex/"
    }
    fun File.readLong(): Long {
        return this.readText().trim().toLong()
    }

    fun readLong(basePath: String, childPath: String): Long {
        return File(basePath, childPath).readLong()
    }
}