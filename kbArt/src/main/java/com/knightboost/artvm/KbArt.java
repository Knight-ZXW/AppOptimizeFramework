package com.knightboost.artvm;

import android.os.Build;

public class KbArt {
    private static boolean inited = false;

    static {
        loadSo();
    }

    public static void loadSo(){
        try {
            if (inited){
                return;
            }
            System.loadLibrary("kbArt");
        }catch (Exception e){
            inited = false;
        }
    }



    public static native boolean nSetJdwpAllowed(boolean allowed);

    public static native boolean nIsJdwpAllow();

    public static native boolean nSetJavaDebuggable(boolean debuggable);

    public static native boolean nDisableClassVerify();

    public static native boolean nDelayJit();

    public static native  boolean nResumeJit();

}
