package com.knightboost.looper.free;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtil {

    public static <T> T getDeclaredFieldObj(Class clazz, Object obj,
                                            String fieldName){
        try {
            Field field = (Field) Class.class.getDeclaredMethod("getDeclaredField", String.class)
                    .invoke(clazz,fieldName);
            field.setAccessible(true);
            return (T) field.get(obj);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static Method getDeclaredMethod(
            Class clazz,String methodName,Class<?>... parameterTypes
    ){
        try {
            Method method = (Method) Class.class.getDeclaredMethod(
                    "getDeclaredMethod",String.class,
                    Class[].class
            ).invoke(clazz,methodName,parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
