package com.knightboost.kprofiler.atrace;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.MainThread;

import com.bytedance.android.bytehook.ByteHook;
import com.bytedance.android.bytehook.ILibLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicBoolean;

public class RheaATrace {
    private static final String TAG = "rhea:atrace";

    private static boolean started = false;

    private static boolean inited = false;
    private static File externalDirectory;

    private static final AtomicBoolean jniLoadSuccess = new AtomicBoolean(false);

    static {
        jniLoadSuccess.set(loadJni());
    }

    @MainThread
    public static boolean start(Context context, File externalDir) {
        externalDirectory = externalDir;
        if (started) {
            Log.d(TAG, "rhea atrace has been started!");
            return true;
        }
        if (!init()) {
            return false;
        }
        if (!externalDir.exists()) {
            if (!externalDir.mkdirs()) {
                Log.e(TAG, "failed to create directory " + externalDir.getAbsolutePath());
                return false;
            }
        }
        BinaryTrace.init(new File(externalDir, "rhea-atrace.bin"));
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
            try {
                writeBinderInterfaceTokens();
            } catch (IOException e) {
                Log.e(TAG, "failed to write binder interface tokens", e);
            }

            if (TraceEnableTagsHelper.updateEnableTags()) {
                started = false;
                return true;
            }
        }
        return false;
    }
    private static void writeBinderInterfaceTokens() throws IOException {
        String[] tokens = nativeGetBinderInterfaceTokens();
        if (tokens == null) {
            Log.e(TAG, "writerBinderInterfaceTokens error. may be oom");
            return;
        }
        try (FileWriter writer = new FileWriter(new File(externalDirectory, "binder.txt"))) {
            long now = SystemClock.uptimeMillis();
            for (String token : tokens) {
                writer.write("#");
                writer.write(token);
                writer.write("\n");
                try {
                    // try $Stub first
                    for (Field field : Class.forName(token + "$Stub").getDeclaredFields()) {
                        if (Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) {
                            appendFieldValue(writer, field);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    try {
                        // then fall back to self
                        for (Field field : Class.forName(token).getDeclaredFields()) {
                            if (Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) {
                                appendFieldValue(writer, field);
                            }
                        }
                    } catch (ClassNotFoundException ignore) {
                    } catch (IllegalAccessException ignore) {
                    }
                } catch (IllegalAccessException ignore) {
                }
            }
            long cost = SystemClock.uptimeMillis() - now;
            Log.d(TAG, "writeBinderInterfaceTokens cost " + cost + "ms");
        }
    }

    private static void appendFieldValue(FileWriter writer, Field field) throws IllegalAccessException, IOException {
        field.setAccessible(true);
        Object value = field.get(null);
        if (value instanceof Integer) {
            writer.write(field.getName());
            writer.write(":");
            writer.write(value.toString());
            writer.write("\n");
        }
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
    public static int getHttpServerPort() {
        return nativeGetHttpServerPort();
    }

    public static boolean isStartWhenAppLaunch() {
        return nativeStartWhenAppLaunch();
    }

    public static boolean isMainThreadOnly() {
        return nativeMainThreadOnly();
    }


    @MainThread
    private static native int nativeStart(String atraceLocation);

    @MainThread
    private static native int nativeStop();

    private static native boolean nativeStartWhenAppLaunch();

    public static native boolean nativeMainThreadOnly();

    private static native int nativeGetArch();

    public static native int nativeGetHttpServerPort();

    private static native String[] nativeGetBinderInterfaceTokens();

    public static int getArch() {
        return nativeGetArch();
    }
}
