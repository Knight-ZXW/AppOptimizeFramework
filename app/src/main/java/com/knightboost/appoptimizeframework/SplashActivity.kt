package com.knightboost.appoptimizeframework

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.knightboost.optimize.looperopt.LooperMsgOptimizeManager
import com.knightboost.optimize.looperopt.MsgOptimizeType

class SplashActivity : AppCompatActivity() {
    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Log.d("MainLooperBoost", "SplashActivity onCreate")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
        };

    }

    override fun onStart() {
        super.onStart()
        Log.d("MainLooperBoost", "SplashActivity onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainLooperBoost", "SplashActivity onResume")
        Handler().postDelayed({
            //发送3秒的耗时消息到队列中
            //这里为了方便模拟，直接在主线程发送耗时任务,模拟耗时消息在 启动Activity消息之前的场景
            handler.post({
                Thread.sleep(100)
                Log.e("MainLooperBoost", "任务处理100ms")
            })
            handler.post({
                Thread.sleep(3000)
                Log.e("MainLooperBoost", "任务处理3000ms")
            })
            val intent = Intent(this, MainActivity::class.java)
            Log.e("MainLooperBoost", "begin start to MainActivity")
            startActivity(intent)
            //标记接下来需要优化 启动Activity的相关消息
            LooperMsgOptimizeManager.getInstance().updateOptimizeType(MsgOptimizeType.TYPE_OPTIMIZE_NEXT_START_ACTIVITY_MSG)
        },1000)


    }

    override fun onPause() {
        super.onPause()
        Log.d("MainLooperBoost", "SplashActivity onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainLooperBoost", "SplashActivity onStop")
    }

}