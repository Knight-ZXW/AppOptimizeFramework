package com.knightboost.appoptimizeframework

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Message
import android.util.Log
import com.knightboost.appoptimizeframework.tests.retrofit.BilibiliService
import com.knightboost.appoptimizeframework.tests.retrofit.HttpService
import com.knightboost.appoptimizeframework.tests.preloadtest.*
import com.knightboost.kprofiler.atrace.RheaATrace
import com.knightboost.optimize.looperopt.LooperMsgOptimizeManager
import com.knightboost.optimize.preload.ClassPreloadExecutor
//import com.knightboost.kprofiler.KProfiler
import com.knightboost.messageobserver.MessageObserver
import com.knightboost.messageobserver.MessageObserverManager
import com.knightboost.optimize.looperopt.StateListener
import org.lsposed.hiddenapibypass.HiddenApiBypass
import timber.log.Timber
import java.io.File
import java.lang.RuntimeException

class MyApp : Application() {
    @SuppressLint("BinaryOperationInTimber")
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        val dir = File(base.externalCacheDir,"trace")
        dir.mkdirs()
        Log.e("zxw","dir is ${dir.absolutePath}")

        RheaATrace.start(baseContext,
        dir)

//        MonitorClassLoader.hook(this,true)
        // 打印 类加载信息
//        MonitorClassLoader.printClassEnable = false

        //hidden api exemption 是必要的
        Timber.plant(Timber.DebugTree())
        MessageObserverManager.setUsePrinterToWatch(true)
//        HiddenApi.getDefault().exempt("Landroid/os/Handler")
//        HiddenApi.getDefault().exempt("Landroid/os/Looper")
//        HiddenApi.getDefault().exempt("Landroid/os/MessageQueue")
//        HiddenApi.getDefault().exempt("Landroid/app/servertransaction")
//        HiddenApi.getDefault().exempt("Landroid/util/BoostFramework")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            HiddenApiBypass.addHiddenApiExemptions("")
//        };

        Log.e("MainLooperBoost","Application attachBaseContext")

        Thread{
            Timber.tag("App").e("开始初始化")
            LooperMsgOptimizeManager.init(this,object :StateListener{
                override fun onUnExpectError(tag: String, msg: String,
                    e: Throwable?) {
                        Timber.tag(tag).e(e,msg)
                }
            })
            LooperMsgOptimizeManager.getInstance().upgradeVsyncByBarrier(true)
            val initSuccess = LooperMsgOptimizeManager.getInstance().isInitSuccess
            Timber.e("LooperMsgOptimizeManager 初始化结果 $initSuccess")
            LooperMsgOptimizeManager.getInstance()
                .setEnable(true)

        }.start()

        ClassPreloadExecutor.addDemander {
            listOf<Class<*>>(
                PreloadClass1::class.java,
                PreloadClass2::class.java,
                PreloadClass3::class.java,
                PreloadClass4::class.java,
                PreloadClass5::class.java,
                PreloadClass6::class.java,
                PreloadClass7::class.java,
                PreloadClass8::class.java,
                PreloadClass9::class.java,
                PreloadClass10::class.java,
            ).toTypedArray()
        }
        Thread{
            ClassPreloadExecutor.doPreload()
        }.start()
        asyncPreParseRetrofitService()
    }

    private fun enableMonitorClassLoad(base: Context) {
//        KProfiler.init(base);
        var file = File(base.cacheDir, "temp.txt")
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        val absolutePath = file.absolutePath
        Log.e("Demo", "类加载文件保存在 ${absolutePath}")
//        KProfiler.startMonitorClassLoad(absolutePath, Thread.currentThread());
    }

    private fun asyncPreParseRetrofitService(){
        //这行是为了避免类加载带来的差异
        HttpService.retrofit.create(BilibiliService::class.java).archiveStat2(0)

//        Thread{
//            val retrofit = HttpService.retrofit
//            RetrofitPreloadUtil.preloadClassMethods(retrofit,
//                BilibiliService::class.java,
//                arrayOf("archiveStat"))
//        }.start()

    }

    override fun onCreate() {
        super.onCreate()
        Log.e("MainLooperBoost","Application onCreate")
    }

}