package com.knightboost.appoptimizeframework

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.webkit.WebView
import com.knightboost.appoptimizeframework.tests.preloadtest.*
import com.knightboost.optimize.looperopt.LooperMsgOptimizeManager
import com.knightboost.optimize.looperopt.MsgOptimizeType

/**
 * i 103-> watch_on_resume  2
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
//        ColdLaunchBoost.getInstance().log("before super onCreate")
        super.onCreate(savedInstanceState)
        classLoadTest()
        setContentView(R.layout.activity_main)
        Log.d("MainLooperBoost","MainActivity onCreate");

    }

    private fun classLoadTest() {
        var begin = SystemClock.elapsedRealtimeNanos()
        PreloadClass1.map1
        PreloadClass2.map1
        PreloadClass3.map1
        PreloadClass4.map1
        PreloadClass5.map1
        PreloadClass6.map1
        PreloadClass7.map1
        PreloadClass8.map1
        PreloadClass9.map1
        PreloadClass10.map1
        var end = SystemClock.elapsedRealtimeNanos()
        Log.e("classPreload", "access classes cost ${(end - begin) / 1000}us")
    }

    override fun onStart() {
        val decorView = window.decorView
        super.onStart()
        Log.d("MainLooperBoost","MainActivity onStart");
        var contentView = findViewById<View>(android.R.id.content)

    }

    override fun onResume() {
        //标记 接下来需要优化 frame消息
        Log.d("MainLooperBoost","MainActivity before super onResume");
        Handler().post {
            Log.e("Launch","主页阶段耗时消息")
            Thread.sleep(2000)
        }
        LooperMsgOptimizeManager.getInstance().updateOptimizeType(MsgOptimizeType.TYPE_OPTIMIZE_NEXT_DO_FRAME)
        super.onResume()
        Log.d("MainLooperBoost","MainActivity onResume");
        window.decorView.post {
            Log.e("Launch","decorView post finish")
        }
        var v:WebView? =null
    }



    private var windowFocusFirstChangeConsume = true;
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (windowFocusFirstChangeConsume){
            Log.e("MainLooperBoost","MainActivity onWindowFocusChanged");
            windowFocusFirstChangeConsume = false;
        }

    }


}