package com.knightboost.artvm;

public class ArtThread {

    static {
        KbArt.loadSo();
    }

    public static native int getTid(Thread thread);
}
