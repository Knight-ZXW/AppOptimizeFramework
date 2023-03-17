package com.knightboost.optimize.util

import java.io.File

object ThreadUtil {

    /**
     * 获取目标线程最后运行在哪个CPU
     */
    fun getLastRunOnCpu(tid:Int):Int{
        var path = "/proc/${android.os.Process.myPid()}/task/${tid}/stat"
        try {
            val content = File(path).readText()
            var arrays = StringUtil.splitWorker(content,' ')
            var cpu = arrays[38]
            return cpu.toInt()
        }catch (e:Exception){
            // this task  may have already ended
            return -1;
        }

    }

    fun getNice(tid:Int):Int{
        var path = "/proc/${android.os.Process.myPid()}/task/${tid}/stat"
        try {
            val content = File(path).readText()
            var arrays = StringUtil.splitWorker(content,' ')
            var cpu = arrays[18]
            return cpu.toInt()
        }catch (e:Exception){
            // this task  may have already ended
            return -1;
        }
    }
}