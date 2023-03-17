package com.knightboost.optimize.cpuboost

import android.content.Context
import android.util.Log
import java.lang.reflect.Method
import java.util.concurrent.CopyOnWriteArrayList

class QcmCpuPerformance : CpuPerformance {

    companion object {

        const val DEBUG = false
        const val TAG = "QcmCpuPerformance"
        /**
         * 是否允许CPU进入深度低功耗模式， 对应 /dev/cpu_dma_latency, 默认空，不允许则设置为1
         */
        const val MPCTLV3_ALL_CPUS_PWR_CLPS_DIS = 0x40400000

        /**
         * 对应控制小核最小频率
         */
        const val MPCTLV3_MIN_FREQ_CLUSTER_LITTLE_CORE_0 = 0x40800100

        /**
         * 对应控制小核最大频率
         */
        const val MPCTLV3_MAX_FREQ_CLUSTER_LITTLE_CORE_0 = 0x40804100

        /**
         * 对应控制大核最小频率
         */
        const val MPCTLV3_MIN_FREQ_CLUSTER_BIG_CORE_0 = 0x40800000

        /**
         * 对应控制大核最大频率
         */
        const val MPCTLV3_MAX_FREQ_CLUSTER_BIG_CORE_0 = 0x40804000

        /**
         * 对应控制超大核最小频率
         */
        const val MPCTLV3_MIN_FREQ_CLUSTER_PLUS_CORE_0 = 0x40800200;

        /**
         * 对应控制超大核最小频率
         */
        const val MPCTLV3_MAX_FREQ_CLUSTER_PLUS_CORE_0 = 0x40804200

        /**
         * 不太清楚，似乎是调度加速
         */
        const val MPCTLV3_SCHED_BOOST = 0x40C00000;

    }

    var initSuccess = false

    lateinit var acquireFunc: Method
    lateinit var perfHintFunc: Method
    lateinit var releaseFunc: Method
    lateinit var frameworkInstance: Any

    var boostHandlers = CopyOnWriteArrayList<Int>()

    /**
     * 配置: 请求将所有CPU核心频率拉满,并禁止进入深入低功耗模式
     */
    private var CONFIGS_FREQUENCY_HIGH = intArrayOf(
        MPCTLV3_ALL_CPUS_PWR_CLPS_DIS, 1,
        MPCTLV3_SCHED_BOOST, 1,
        MPCTLV3_MIN_FREQ_CLUSTER_BIG_CORE_0, 0xFFF,
        MPCTLV3_MIN_FREQ_CLUSTER_LITTLE_CORE_0, 0xFFF,
        MPCTLV3_MIN_FREQ_CLUSTER_PLUS_CORE_0, 0xFFF,
        MPCTLV3_MAX_FREQ_CLUSTER_BIG_CORE_0, 0xFFF,
        MPCTLV3_MAX_FREQ_CLUSTER_LITTLE_CORE_0, 0xFFF,
        MPCTLV3_MAX_FREQ_CLUSTER_PLUS_CORE_0, 0xFFF,
    )

    var DISABLE_POWER_COLLAPSE = intArrayOf(MPCTLV3_ALL_CPUS_PWR_CLPS_DIS, 1)

    /**
     * 初始化CpuBoost 核心功能
     */
    override fun init(context: Context): Boolean {
        try {
            val boostFrameworkClass = Class.forName("android.util.BoostFramework")

            val constructor = boostFrameworkClass.getConstructor(Context::class.java)
                ?: return false

            frameworkInstance = constructor.newInstance(context)

            acquireFunc = boostFrameworkClass.getDeclaredMethod(
                "perfLockAcquire", Integer.TYPE, IntArray::class.java
            )

            perfHintFunc = boostFrameworkClass.getDeclaredMethod(
                "perfHint", Int::class.javaPrimitiveType, String::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType
            )

            releaseFunc = boostFrameworkClass.getDeclaredMethod(
                "perfLockReleaseHandler", Integer.TYPE
            )
            initSuccess = true
            return true
        } catch (e: Exception) {
            initSuccess = false
            CpuBoostManager.boostErrorLog(TAG, "Init failed", e)
            return false
        }
    }

    /**
     * 提升所有核心CPU频率到最高频率
     */
    override fun boostCpu(duration: Int): Boolean {
        if (!initSuccess) return false
        return try {
            perfLockAcquire(duration, DISABLE_POWER_COLLAPSE)
            perfLockAcquire(duration, CONFIGS_FREQUENCY_HIGH)
            return true
        } catch (e: Exception) {
            CpuBoostManager.boostErrorLog(TAG, "boostCpuFailed", e)
            false
        }
    }

    override fun boostCpu(duration: Int, commands: IntArray): Boolean {
        if (!initSuccess) return false
        return try {
            perfLockAcquire(duration, commands)
            return true
        } catch (e: Exception) {
            CpuBoostManager.boostErrorLog(TAG, "boostCpuFailed", e)
            false
        }
    }

    /**
     *   Toggle off all optimizations requested Immediately.
     *   Use this function if you want to release before the time duration ends.
     *
     *   这个函数并不强制调用，只用于提前取消所有已配置的加速效果。
     */
    override fun stopBoost() {
        val handlers = boostHandlers.toTypedArray()
        for (handler in handlers) {
            try {
                releaseFunc.invoke(frameworkInstance, handler)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Toggle on all optimizations requested.
     * @param duration: The maximum amount of time required to hold the lock.
     *       Only a positive integer value in milliseconds will be accepted.
     *       You may explicitly call perfLockRelease before the timer expires.
     * @param list Enter all optimizations required. Only the optimizations in the
     *       table below are supported. You can only choose one optimization
     *       from each of the numbered sections in the table. Incorrect or
     *       unsupported optimizations will be ignored.
     *
     *       NOTE: Enter the optimizations required in the order they appear in the table.
     */
    private fun perfLockAcquire(duration: Int, list: IntArray): Int {
        val handler = acquireFunc.invoke(frameworkInstance, duration, list) as Int;
        if (handler > 0) {
            boostHandlers.add(handler)
        }
        return handler
    }

}