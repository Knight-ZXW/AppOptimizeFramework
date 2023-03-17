package com.knightboost.appoptimizeframework.tests.preloadtest;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class PreloadClass7 {
    public static final Map map1 = new HashMap();
    public static final Map map2 = new HashMap();
    public static final Map map3 = new HashMap();
    public static final Map map4 = new HashMap();
    public static final Map map5 = new HashMap();
    private static boolean b1 =false;
    private static boolean b2 =false;
    private static boolean b3 =false;
    private static boolean b4 =false;
    private static boolean b5 =false;
    static {
        Log.d("PreloadClass","PreloadClass7 is loaded");
    }
}
