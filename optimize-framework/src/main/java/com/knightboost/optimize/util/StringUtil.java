package com.knightboost.optimize.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

    public static String[] splitWorker(String str, char separatorChar){
        return  splitWorker(str,separatorChar,false);
    }

    public static String[] splitWorker(String str, char separatorChar, boolean preserveAllTokens) {

        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return new String[]{};
        }
        List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match || preserveAllTokens) {
                    list.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }
                start = ++i;
                continue;
            }
            lastMatch = false;
            match = true;
            i++;
        }
        if (match || (preserveAllTokens && lastMatch)) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }
}
