package com.knightboost.artvm;

import java.lang.reflect.Field;

public class ArtThread {

    static {
        KbArt.loadSo();
    }

    public static native int getTid(Thread thread);

    public static native long getCpuMicroTime(long nativePeer);

    public static long getNativePeer(Thread thread){
        try {
            Field nativePeer = Thread.class.getDeclaredField("nativePeer");
            nativePeer.setAccessible(true);
            long nativePeerValue = (long) nativePeer.get(thread);
            return nativePeerValue;
        } catch (Exception e) {
            return -1;
        }
    }
}
