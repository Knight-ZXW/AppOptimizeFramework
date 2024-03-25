package com.knightboost.appoptimizeframework

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
//import com.bytedance.rheatrace.core.TraceApplicationLike
import com.bytedance.shadowhook.ShadowHook
import com.knightboost.appoptimizeframework.tests.retrofit.BilibiliService
import com.knightboost.appoptimizeframework.tests.retrofit.HttpService
import com.knightboost.messageobserver.MessageObserverManager
import com.knightboost.optimize.looperopt.LooperMsgOptimizeManager
import com.knightboost.optimize.looperopt.StateListener
import com.knightboost.optimize.preload.ClassPreloadExecutor
import timber.log.Timber


class MyApp : Application() {
    @SuppressLint("BinaryOperationInTimber")
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
//        TraceApplicationLike.attachBaseContext(base)
        try {

        }catch (e:java.lang.Exception){
            e.stackTraceToString()
        }
        ShadowHook.init(
            ShadowHook.ConfigBuilder()
                .setMode(ShadowHook.Mode.UNIQUE)
                .build()
        )
//        val dir = File(base.externalCacheDir,"trace")
//        dir.mkdirs()
//        RheaATrace.start(baseContext,
//        dir)



        //hidden api exemption 是必要的
        Timber.plant(Timber.DebugTree())
        MessageObserverManager.setUsePrinterToWatch(true)

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


        Thread{
            ClassPreloadExecutor.doPreload()
        }.start()
        asyncPreParseRetrofitService()
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("MainLooperBoost","Application onCreate")
    }

    private fun asyncPreParseRetrofitService(){
        //这行是为了避免类加载带来的差异
        HttpService.retrofit.create(BilibiliService::class.java).archiveStat2(0)

    }



}