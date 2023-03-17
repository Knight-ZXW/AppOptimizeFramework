package com.knightboost.appoptimizeframework

import android.os.*
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.knightboost.appoptimizeframework.cpu.CpuReadUtil
import com.knightboost.optimize.util.ThreadUtil
import com.knightboost.appoptimizeframework.databinding.FragmentMainBinding
import com.knightboost.appoptimizeframework.gsonopttest.GsonTest
import com.knightboost.appoptimizeframework.tests.retrofit.BilibiliService
import com.knightboost.appoptimizeframework.tests.retrofit.HttpService
import com.knightboost.artvm.ArtThread
import com.knightboost.artvm.KbArt
import com.knightboost.kprofiler.atrace.RheaATrace
import com.knightboost.messageobserver.MessageObserver
import com.knightboost.messageobserver.MessageObserverManager
import com.knightboost.optimize.cpuboost.*
import com.knightboost.optimize.looperopt.LooperMsgOptimizeManager
import kotlin.concurrent.thread

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instanceRef of this fragment.
 */
class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        CpuBoostManager.init(requireContext())
    }

    var cpuPrintStart = false;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        LooperMsgOptimizeManager.getInstance().log("main Fragment onCreateView")
//        return inflater.inflate(R.layout.fragment_main, container, false)
        binding = FragmentMainBinding.inflate(inflater)
        return binding.root
    }

    private fun retrofitTest() {
        val retrofit = HttpService.retrofit
        val begin1 = SystemClock.elapsedRealtimeNanos()
        val bilibiliService = retrofit.create(BilibiliService::class.java)
        val end1 = SystemClock.elapsedRealtimeNanos()
        Log.w("retrofitTest", "创建BilibiliService动态代理实例耗时 ${(end1 - begin1) / 1000} us")
        Log.w("retrofitTest", "开始调用 archiveStat")
        val begin = SystemClock.elapsedRealtimeNanos()
        bilibiliService.archiveStat(170001)
        val end = SystemClock.elapsedRealtimeNanos()
        Log.w("retrofitTest", "get archiveStat method cost ${(end - begin) / 1000} us")
    }

    override fun onResume() {
        super.onResume()

        binding.btnRetrofitTest.setOnClickListener { retrofitTest() }
        binding.btnGsonTest.setOnClickListener {
//            gsonTest()
            MessageObserverManager.getMain().addMessageObserver(object : MessageObserver {
                override fun onMessageDispatchStarting(msg: String?) {
                }

                override fun onMessageDispatched(msg: String?, message: Message?) {
                    Log.e("zxwTest","onMessageDispatched "+message)
                }
            })

            val handler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
//                    throw RuntimeException("测试")
                }
            }
            handler.sendEmptyMessage(100)

        }
        binding.btnCpuBoost.setOnClickListener { cpuFrequencyBoostTest() }

        binding.btnCpuBindCore.setOnClickListener {
            threadCpuBindTest()
        }

        binding.btnGetMainThreadAffinity.setOnClickListener {

            var mainThreadId = ArtThread.getTid(Looper.getMainLooper().thread)
            Log.e("cpuBind", "主线程tid ${mainThreadId}  pid ${Process.myPid()}")
            var cpuAffinity = ThreadCpuAffinityManager.getCpuAffinity(mainThreadId)
            Log.d("cpuBind", "-> 主线程 CPU亲和性相关 -> ${cpuAffinity.joinToString(",")}")
        }

        binding.btnThreadPriorityTest.setOnClickListener {
            threadPriorityTest()
        }

        binding.jdwpTest.setOnClickListener {

            KbArt.nSetJavaDebuggable(true)
            KbArt.nSetJdwpAllowed(true)
            val nIsJdwpAllow = KbArt.nIsJdwpAllow()
            Log.e("art", "nIsJdwpAllow ${nIsJdwpAllow}")
//            KProfiler.init(context)
//            KProfiler.testMethodTrace()
        }
        val stopSuccess = RheaATrace.stop()
        Log.e("zxw","stopTrace $stopSuccess")
    }

    /**
     * Cpu提频 测试
     */
    private fun cpuFrequencyBoostTest() {
        thread {
            Log.w("cpuFreq", "提频前↓")
            for (i in 0 until 3) {
                CpuReadUtil.printAllCpuFreq()
                Thread.sleep(1000)
            }
            Log.w("cpuFreq", "开始提频5秒↓")
            CpuBoostManager.boostCpu(5_000, "test")
            Log.w("cpuFreq", "提频后↓")
            for (i in 0 until 5) {
                CpuReadUtil.printAllCpuFreq()
                Thread.sleep(900)
            }
            Log.w("cpuFreq", "提频时间结束后↓")
            for (i in 0 until 3) {
                CpuReadUtil.printAllCpuFreq()
                Thread.sleep(1000)
            }
        }
    }

    /**
     * 线程优先级测试
     *
     */
    private fun threadPriorityTest() {
//        Thread{
//            var currentThread = Thread.currentThread()
//            var tid = ArtThread.getTid(currentThread)
//            Log.e("priorityTest","当前线程 $tid" +
//                    " java优先级 ${currentThread.priority} nice值 ${ThreadUtil.getNice(tid)}")
//            currentThread.priority=Thread.MAX_PRIORITY;
//            Log.e("priorityTest","使用 Thread.setPriority 设置最高优级10 后  nice值 ${ThreadUtil.getNice(tid)}")
//            Process.setThreadPriority(tid,-20)
//            Log.e("priorityTest","使用 Process.setThreadPriority 设置最高优级-20 后  nice值 ${ThreadUtil.getNice(tid)}")
//        }.start()

        startTask("task1", false);
        startTask("task2", false);
        startTask("task3", false);
        startTask("task4", true);

    }

    private fun startTask(taskName: String, upgradePriority: Boolean) {
        val thread = Thread {
            //将工作线程绑定在同一个CPU上
            val tid = ArtThread.getTid(Thread.currentThread())
            Log.e(
                "threadPriority", "线程${taskName} 刚开始运行在 ${ThreadUtil.getLastRunOnCpu(tid)}," +
                        "亲和性 ${ThreadCpuAffinityManager.getCpuAffinity(tid).joinToString(",")}"
            )
            ThreadCpuAffinityManager.setCpuAffinityToThread(Thread.currentThread(), intArrayOf(7))
            if (upgradePriority) {
                Thread.currentThread().priority = Thread.MAX_PRIORITY
//                Process.setThreadPriority(tid,-20)
            }
            val beginCpuTime = SystemClock.currentThreadTimeMillis()
            val beginTime = SystemClock.elapsedRealtime()
            var timeOut = false;
            while (!timeOut) {
                if (SystemClock.elapsedRealtime() - beginTime > 5000) {
                    timeOut = true
                }
            }

            val endCpuTime = SystemClock.currentThreadTimeMillis()
            val endTime = SystemClock.elapsedRealtime()

            Log.e(
                "threadPriority", "线程${taskName} 经过 ${endTime - beginTime}秒" +
                        " 实际获得执行的cpu时间 ${endCpuTime - beginCpuTime} ,最后一次运行在CPU ${ThreadUtil.getLastRunOnCpu(tid)}"
            )
        }
        thread.name = taskName
        thread.start()
    }

    private fun gsonTest() {
        GsonTest.gson
        GsonTest.gson2
        //↑ 避免 初始化耗时影响数据测试
        val begin = SystemClock.elapsedRealtime()
        for (i in 1..10) {
            GsonTest.test()
        }
        val end = SystemClock.elapsedRealtime()
        Log.e("gsonTest", "优化前 总耗时 ${end - begin}")

        val begin1 = SystemClock.elapsedRealtime()
        for (i in 1..10) {
            GsonTest.testWithCustomTypeAdapter()
        }
        val end1 = SystemClock.elapsedRealtime()
        Log.e("gsonTest", "优化后 总耗时 ${end1 - begin1}")
    }

    companion object {
        /**
         * Use this factory method to create a new instanceRef of
         * this fragment using the provided parameters.
         * @return A new instanceRef of fragment MainFragment.
         */
        @JvmStatic fun newInstance() =
            MainFragment()
    }

    fun threadCpuBindTest() {
        val newThread = Thread {
            val begin = SystemClock.elapsedRealtime()
            Thread.sleep(1000L)
            while ((SystemClock.elapsedRealtime() - begin) < 30_000) {
                val a = 10000.0
                val b = 20000.0
                val c = a * b
                val d = a / b
                val e = a + b
                val f = a - b
            }
            Log.e("cpuBind", "工作线程${Thread.currentThread().id} 结束")
        }
        newThread.start()

        thread {
            val targetThread = newThread
            var targetTid = ArtThread.getTid(targetThread)
            val TAG = "cpuBind"
            Log.e(TAG, "目标线程tid" + targetTid)
            var cpuAffinity = ThreadCpuAffinityManager.getCpuAffinity(targetTid)
            val cpus = binding.bindCpu.text.split(" ").map { it.toInt() }.toIntArray()
            Log.d(TAG, "-> 目标线程 CPU亲和性相关 -> ${cpuAffinity.joinToString(",")}")

            val commandExecutor = Runtime.getRuntime().exec("taskset -p 0x3 ${Process.myPid()}")
            val exitValue = commandExecutor.waitFor()

            Log.e(TAG, "命令执行结果 ${exitValue}")
            for (i in 0 until 5) {
                Thread.sleep(200)
                Log.d(TAG, "目标线程 目前运行在CPU ${ThreadUtil.getLastRunOnCpu(targetTid)}")
            }
            var isSuccess = ThreadCpuAffinityManager.setCpuAffinityToThread(targetThread, cpus)
            Log.d(
                TAG,
                "Cpu亲和性尝试修改为 ${cpus.joinToString(" ")}, 结果 $isSuccess ,读取最新affinity结果${
                    ThreadCpuAffinityManager.getCpuAffinity(targetTid)
                        .joinToString(" ")
                }"
            )
            Thread.sleep(50)
            for (i in 0 until 20) {
                Thread.sleep(200)
                Log.d(TAG, "目标线程 目前运行在CPU ${ThreadUtil.getLastRunOnCpu(targetTid)}")
            }
            isSuccess = ThreadCpuAffinityManager.resetCpuAffinity(targetThread)
            Log.d("cpuBind", "重置CPU亲和性 " + isSuccess)
            Thread.sleep(50)
            cpuAffinity = ThreadCpuAffinityManager.getCpuAffinity(targetTid)
            Log.d("cpuBind", "-> 目标线程 CPU亲和性相关 -> ${cpuAffinity.joinToString(",")}")
        }

    }
}