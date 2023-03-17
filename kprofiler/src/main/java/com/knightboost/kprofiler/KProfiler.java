package com.knightboost.kprofiler;

import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KProfiler {

    private static volatile boolean inited = false;

    private static volatile boolean soLoad =false;
    private static final String TAG = "KProfiler";

    private static final String LIB_NAME = "kprofiler";
    private static String packageCodePath = "";

    public static boolean isEnable(){
        return  inited;
    }

    public static synchronized void init(Context context){
        if (inited){
            return;
        }
        loadSo(context);
        KLogger.e(TAG,"KProfiler 初始化成功");
        inited = true;
    }

    public static synchronized void loadSo(Context context){
        if (soLoad){
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            KLogger.e(TAG,"KProfiler初始化失败：最低支持 Android 8.0!");
            return;
        }
        String path = copyAgentSo(context);
        if (path == null){
            KLogger.e(TAG,"copy jvmti agent so failed ");
            return;
        }
        //todo 放到assets?
        KLogger.e(TAG,"copy jvmti agent so to "+path);
        System.load(path);
        attachJvmtiAgent(path,context.getClassLoader());
        soLoad = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Keep
    private static String copyAgentSo(Context context){
        try {
            packageCodePath = context.getPackageCodePath();
            ClassLoader classLoader = context.getClassLoader();
            Method findLibrary = ClassLoader.class.getDeclaredMethod("findLibrary", String.class);
            String jvmtiAgentLibPath = (String) findLibrary.invoke(classLoader, LIB_NAME);

            //copy lib to /data/user/0/com.adi.demo/files/adi/agent.so
            Log.d(TAG, "kprofiler path " + jvmtiAgentLibPath);
            if (jvmtiAgentLibPath == null){
                KLogger.e(TAG,"did find kprofiler so");
                return null;
            }

            File filesDir = context.getFilesDir();
            File jvmtiLibDir = new File(filesDir, "kprofiler");
            if (!jvmtiLibDir.exists()) {
                jvmtiLibDir.mkdirs();
            }
            File agentLibSo = new File(jvmtiLibDir, "kprofiler.so");
            //todo 判断是否需要变更
            if (agentLibSo.exists()) {
                agentLibSo.delete();
            }
            Files.copy(Paths.get(new File(jvmtiAgentLibPath).getAbsolutePath()),
                    Paths.get((agentLibSo).getAbsolutePath()));

            Log.d(TAG, agentLibSo.getAbsolutePath() + ", " + packageCodePath);
            return agentLibSo.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG,"copy agent so failed");
            e.printStackTrace();
            return null;
        }
    }


    private static boolean attachJvmtiAgent(String agentPath,ClassLoader classLoader){
        boolean attachSuccess =false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                Debug.attachJvmtiAgent(agentPath,null,classLoader);
            }else {
                Class vmDebugClazz = Class.forName("dalvik.system.VMDebug");
                Method attachAgentMethod = vmDebugClazz.getMethod("attachAgent", String.class);
                attachAgentMethod.setAccessible(true);
                attachAgentMethod.invoke(null, agentPath);
            }
            attachSuccess = true;
        }catch (Exception e){
            Log.e(TAG,"attachAgent failed" ,e);
            attachSuccess = false;
        }
        return attachSuccess;
    }


    public static  boolean startMonitorClassLoad(String filePath){
        if (inited){
            return false;
        }
        return nStartMonitorClassLoad(filePath);
    }


    public static native boolean nStartMonitorClassLoad(String filePath);

    public static  boolean startMonitorClassLoad(String filePath,Thread targetThread){
        if (inited){
            return false;
        }
        return nStartMonitorClassLoadOfThread(filePath,targetThread);
    }

    public static native boolean nStartMonitorClassLoadOfThread(String filePath,
                                                               Thread targetThread);

    public static  void recordClassLoadMsg(String msg){
        if (!inited){
            return;
        }
        nRecordClassLoadMsg(msg);
    }

    /**
     * 该函数用来在记录类加载过程中 标记一些时间节点，用于分隔不同阶段
     * @param msg
     */
    public static native void nRecordClassLoadMsg(String msg);

    public static  boolean stopMonitorClassLoad(){
        if (!inited){
            return false;
        }
        return nStopMonitorClassLoad();
    };

    public static native boolean nStopMonitorClassLoad();


    public static native void testMethodTrace();

}
