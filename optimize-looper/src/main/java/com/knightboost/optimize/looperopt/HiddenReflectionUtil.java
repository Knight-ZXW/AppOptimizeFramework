package com.knightboost.optimize.looperopt;

import androidx.annotation.RestrictTo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class HiddenReflectionUtil {

    private static boolean initSuccess = false;

    private static Method metaGetDeclaredField;

    private static Method metaGetDeclaredMethod;
    private static Method metaClassForNameMethod;

    static {
        try {
            metaGetDeclaredField = Class.class.getDeclaredMethod("getDeclaredField", String.class);
            metaGetDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
            metaClassForNameMethod = Class.class.getDeclaredMethod("forName",String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            initSuccess = false;
        }
        initSuccess = true;
    }

    public static Field getDeclaredField(Class clazz, String name) throws Exception {
        if (initSuccess) {
            return (Field) metaGetDeclaredField.invoke(clazz, name);
        } else {
            return clazz.getDeclaredField(name);
        }
    }

    public static Method getDeclaredMethod(Class clazz, String name, Class... parameterTypes) throws Exception {
        if (initSuccess) {
            return (Method) metaGetDeclaredMethod.invoke(clazz, name, parameterTypes);
        } else {
            return clazz.getDeclaredMethod(name, parameterTypes);
        }
    }

    public static Class  forName(String clazzName) throws Exception {
        if (initSuccess){
            return (Class) metaClassForNameMethod.invoke(null,clazzName);
        } else {
            return Class.forName(clazzName);
        }
    }

}
