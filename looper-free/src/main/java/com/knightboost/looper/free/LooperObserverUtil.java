package com.knightboost.looper.free;

import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Method;

public class LooperObserverUtil {
    private static final String TAG = "LooperUtil";

    public static boolean setObserver(final LooperMessageObserver looperMessageObserver) {
        try {
            final Looper.Observer oldObserver = ReflectUtil.getDeclaredFieldObj(Looper.class,null,"sObserver");
            Method forNameMethod = Class.class.getDeclaredMethod("forName", String.class);
            Class classObserver = (Class) forNameMethod.invoke(null, "android.os.Looper$Observer");
            Method setObserverMethod = ReflectUtil.getDeclaredMethod(Looper.class,"setObserver",classObserver);

            //检查函数签名是否符合预期
            Method messageDispatchStarting =ReflectUtil.getDeclaredMethod(classObserver,
                    "messageDispatchStarting");
            Method messageDispatched =ReflectUtil.getDeclaredMethod(classObserver,
                    "messageDispatched",Object.class, Message.class);
            Method dispatchingThrewException =ReflectUtil.getDeclaredMethod(classObserver,
                    "dispatchingThrewException",Object.class, Message.class, Exception.class);
            if (messageDispatchStarting == null || messageDispatched == null ||
                    dispatchingThrewException == null) {
                return false;
            }

            //验证函数是否可调用
            try {
                Looper.Observer observer = new Looper.Observer() {
                    @Override
                    public Object messageDispatchStarting() {
                        return null;
                    }

                    @Override
                    public void messageDispatched(Object token, Message msg) {

                    }

                    @Override
                    public void dispatchingThrewException(Object token, Message msg, Exception exception) {

                    }
                };
                observer.messageDispatchStarting();
                observer.dispatchingThrewException(null,null,null);
                observer.messageDispatched(null,null);
            }catch (IncompatibleClassChangeError | Exception e){
                Log.w(TAG,"use Observer to watching Looper failed",e);
                return false;
            }

            //检查  Observer的函数是否存在
            if (oldObserver!=null){
                setObserverMethod.invoke(null,new Looper.Observer(){
                    @Override
                    public Object messageDispatchStarting() {
                        Object token = null;
                        token = oldObserver.messageDispatchStarting();
                        looperMessageObserver.messageDispatchStarting(token);
                        return token;
                    }

                    @Override
                    public void messageDispatched(Object token, Message msg) {
                        oldObserver.messageDispatched(token, msg);
                        looperMessageObserver.messageDispatched(token, msg);
                    }

                    @Override
                    public void dispatchingThrewException(Object token, Message msg, Exception exception) {
                        oldObserver.dispatchingThrewException(token, msg, exception);
                        looperMessageObserver.dispatchingThrewException(token, msg, exception);
                    }
                });

            }else {
                setObserverMethod.invoke(null,new Looper.Observer(){
                    @Override
                    public Object messageDispatchStarting() {
                        return looperMessageObserver.messageDispatchStarting(new Object());
                    }

                    @Override
                    public void messageDispatched(Object token, Message msg) {
                        looperMessageObserver.messageDispatched(token,msg);

                    }

                    @Override
                    public void dispatchingThrewException(Object token, Message msg, Exception exception) {
                        looperMessageObserver.dispatchingThrewException(token,msg,exception);
                    }
                });
            }


        } catch (Exception e) {
            Log.w(TAG,"set or replace Observer to watching Looper failed",e);
            return false;
        }
        return true;
    }

}
