package com.bytedance.rheatrace.processor;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTraceUtil {
    public static final String stackTraceToString(Throwable t){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
