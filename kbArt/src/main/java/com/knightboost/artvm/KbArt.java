package com.knightboost.artvm;

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
}
