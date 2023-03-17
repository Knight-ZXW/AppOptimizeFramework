package com.knightboost.kprofiler.atrace;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.MainThread;

import com.bytedance.android.bytehook.ByteHook;
import com.bytedance.android.bytehook.ILibLoader;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class RheaATrace {
    private static final String TAG = "rhea:atrace";

    private static boolean started = false;

    private static boolean inited = false;
    private static File externalDirectory;

    private static final AtomicBoolean jniLoadSuccess = new AtomicBoolean(false);


    @MainThread
    public static boolean start(Context context, File externalDir) {
        externalDirectory = externalDir;
        if (started) {
            Log.d(TAG, "rhea atrace has been started!");
            return true;
        }

        if (!externalDir.exists()) {
            if (!externalDir.mkdirs()) {
                Log.e(TAG, "failed to create directory " + externalDir.getAbsolutePath());
                return false;
            }
        }
        if (!init()) {
            return false;
        }
        int resultCode = nativeStart(externalDir.getAbsolutePath());

        if (resultCode != 1) {
            Log.d(TAG, "failed to start rhea-trace, errno: " + resultCode);
        } else {
            if (TraceEnableTagsHelper.updateEnableTags()) {
                started = true;
                return true;
            }
        }
        return false;
    }

    @MainThread
    public static boolean stop() {
        if (!started) {
            Log.d(TAG, "rhea atrace has not been started!");
            return true;
        }
        int resultCode = nativeStop();
        if (resultCode != 1) {
            Log.d(TAG, "failed to stop rhea-trace, errno: " + resultCode);
        } else {
            if (TraceEnableTagsHelper.updateEnableTags()) {
                started = false;
                return true;
            }
        }
        return false;
    }

    private static boolean init() {
        if (inited) {
            return true;
        }
        if (!loadJni()) {
            return false;
        }
        int retCode = ByteHook.init(new ByteHook.ConfigBuilder().setLibLoader(new ILibLoader() {
            @Override
            public void loadLibrary(String libName) {
                Log.e("zxw","to load lib "+libName);
                RheaATrace.loadLib(libName);
            }
        }).build());
        if (retCode != 0) {
            Log.d(TAG, "bytehook init failed, errno: " + retCode);
            return false;
        }
        inited = true;
        return true;
    }
    private static boolean loadJni() {
        if (jniLoadSuccess.get()) {
            return true;
        }
        System.loadLibrary("kprofiler");
        return true;
    }

    @Keep
    private static void loadLib(String libName) {
        try {
            System.loadLibrary(libName);
        } catch (Throwable e) {
            throw new UnsatisfiedLinkError("failed to load bytehook lib:" + libName);
        }
    }

    @MainThread
    private static native int nativeStart(String atraceLocation);

    @MainThread
    private static native int nativeStop();
    private static native int nativeGetArch();

    public static int getArch() {
        return nativeGetArch();
    }
}
