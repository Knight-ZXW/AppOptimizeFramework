package com.knightboost.optimize.looperopt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;
import android.view.Choreographer;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.knightboost.messageobserver.MessageObserverManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import timber.log.Timber;

public class LooperMsgOptimizeManager {
    public static boolean DEBUG = false;

    private MessageOptHandler messageOptHandler;
    private Field field_mHandlerOfViewRootImpl;
    private Handler handlerOfViewRoot;
    private Field field_nextMessage;
    private static final String TAG = "MsgOptimizeManager";

    private Class<?> class_ActivityThread;
    private Class<?> class_ViewRootImpl;
    private Class<?> class_Handler;
    private Class<?> class_MessageQueue;
    private Class<?> class_Message;
    private Handler mh;
    private Choreographer choreographer;
    private Handler choreographerHandler;
    private MessageQueue mhHandlerMessageQueue;
    private Field filed_mMessages;
    private Field field_next;

    @NonNull
    private final StateListener stateListener;
    private boolean enable = false;

    private boolean debug = false;

    private int optimizeType = MsgOptimizeType.TYPE_NONE;

    private static LooperMsgOptimizeManager instance = null;

    private boolean initFailed = false;

    private boolean haveTryToGetChoreographerHandler = false;

    private boolean upgradeVsyncByBarrier = false;



    public static synchronized void init(Context context, StateListener errorCatcher) {
        if (instance == null) {
            instance = new LooperMsgOptimizeManager(context, errorCatcher);
        }
    }

    public static LooperMsgOptimizeManager getInstance() {
        return instance;
    }

    public boolean isInitSuccess() {
        return !initFailed;
    }

    /**
     * @param enable 更新状态为是否启用
     * @return 注意这里的返回值表示更新状态是否成功
     */
    public boolean setEnable(boolean enable) {
        if (initFailed) {
            Timber.tag(TAG).e("update enable state failed,the initialization of coldLaunchBoost SDK is failed");
            return false;
        }
        if (enable == this.enable) { //already in same state
            return true;
        }

        try {
            if (!enable) {
                MessageObserverManager.getMain().removeMessageObserver(messageOptHandler);
            } else {
                MessageObserverManager.getMain().addMessageObserver(messageOptHandler);
            }
        } catch (Exception e) { //由于上面访问了hidden Api ，所以这里可能有异常
            stateListener.onUnExpectError(TAG, "set enable " + enable + " failed", e);
            return false;
        }
        this.enable = enable;
        return true;
    }

    public boolean isEnabled() {
        return enable;
    }

    private LooperMsgOptimizeManager(Context context,
                                     StateListener errorCatcher) {
        //todo 前面加的操作
        if (errorCatcher == null) {
            errorCatcher = new StateListener() {
                @Override
                public void onUnExpectError(@NonNull String tag, @NonNull String msg,
                                            @Nullable Throwable e) {
                    Timber.tag(tag)
                            .e(e, msg);
                }
            };
        }
        this.stateListener = errorCatcher;
        try {
            class_Handler = Class.forName("android.os.Handler");
            class_MessageQueue = Class.forName("android.os.MessageQueue");
            filed_mMessages = class_MessageQueue.getDeclaredField("mMessages");
            filed_mMessages.setAccessible(true);
            field_nextMessage = Message.class.getDeclaredField("next");
            field_nextMessage.setAccessible(true);

            class_Message = Class.forName("android.os.Message");
            field_next = class_Message.getDeclaredField("next");
            field_next.setAccessible(true);
            class_ActivityThread = Class.forName("android.app.ActivityThread");

            //跳转优化所需
            mh = reflectGetmH();
            mhHandlerMessageQueue = getMessageQueue(mh);
            messageOptHandler = new MessageOptHandler(this);
        } catch (Exception e) {
            initFailed = true;
            this.stateListener.onUnExpectError(TAG, "initFailed", e);
        }
    }

    public int getOptimizeType() {
        return optimizeType;
    }


    public boolean updateOptimizeType(int optimizeType) {
        if (!enable) {
            this.optimizeType = MsgOptimizeType.TYPE_NONE;
            Timber.e("update optimize type failed, current state is not enable");
            return false;
        }
        //TODO  根据 optimizeType  以及对应反射信息的结果， 及时返回状态信息
        //同时记录 上次的optimizeType ，如果上次不为none, 需要打印日志
        this.optimizeType = optimizeType;
        return true;
    }


    /**
     * 是否使用屏障提升的方式来优化帧绘制
     */
    public void upgradeVsyncByBarrier(boolean upgradeVsyncByBarrier){
        this.upgradeVsyncByBarrier =upgradeVsyncByBarrier;
    }

    public boolean isUpgradeVsyncByBarrier(){
        return this.upgradeVsyncByBarrier;
    }

    public void clearState() {
        this.optimizeType = MsgOptimizeType.TYPE_NONE;
    }

    public Handler getChoreographerHandler() {
        if (!haveTryToGetChoreographerHandler) {
            try {
                boolean isOnMainThread = Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId();
                if (isOnMainThread) {
                    choreographer = Choreographer.getInstance();
                } else {
                    //may filed because blockedPrivateApi rule
                    try {
                        @SuppressLint("SoonBlockedPrivateApi")
                        Method methodGetMainThreadInstance
                                = Choreographer.class.getDeclaredMethod("getMainThreadInstance");
                        choreographer = (Choreographer) methodGetMainThreadInstance.invoke(null);
                    } catch (Exception e) {
                        Timber.e(e, "you must Call this method on main Thread");
                        throw e;
                    }
                }
                Class<?> class_Choreographer = Class.forName("android.view.Choreographer");
                @SuppressLint("SoonBlockedPrivateApi")
                Field mHandlerField = HiddenReflectionUtil.getDeclaredField(class_Choreographer,"mHandler");
                mHandlerField.setAccessible(true);
                choreographerHandler = (Handler) mHandlerField.get(choreographer);
            } catch (Exception e) {
                stateListener.onUnExpectError(TAG, "getChoreographerHandler failed", e);
            } finally {
                haveTryToGetChoreographerHandler = true;
            }
        }
        return choreographerHandler;
    }

    /**
     * TODO 优化为 lazy get
     *
     * @return
     */
    public MessageQueue getMessageQueue() {
        return mhHandlerMessageQueue;
    }

    private Handler reflectGetmH() throws Exception {
        Object currentActivityThread = class_ActivityThread.getDeclaredMethod("currentActivityThread").invoke(null);
        Field mhField = class_ActivityThread.getDeclaredField("mH");
        mhField.setAccessible(true);
        return (Handler) mhField.get(currentActivityThread);
    }

    public Handler getmH() {
        return mh;
    }

    /**
     * warn: 这里访问了hidden api
     *
     * @param handler
     * @return
     * @throws Exception
     */
    private MessageQueue getMessageQueue(Handler handler) throws Exception {
        Field mQueueField = HiddenReflectionUtil.getDeclaredField(this.class_Handler, "mQueue");
        mQueueField.setAccessible(true);
        return (MessageQueue) mQueueField.get(handler);
    }

    private Message nextMessage(Message message) {
        try {
            Message next = (Message) field_next.get(message);
            return next;
        } catch (IllegalAccessException e) {
            //report it
            return null;
        }

    }

    @SuppressLint("DiscouragedPrivateApi")
    public boolean setMessageNext(Message target, Message nextMessage) {
        try {
            field_nextMessage.set(target, nextMessage);
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    private Message hasMessage(Handler handler, Message head, int what) {
        Message cur = head;
        while (cur != null) {
            if (cur.what == what && cur.getTarget() == handler) {
                return cur;
            }
            cur = nextMessage(cur);
        }
        return null;

    }

    public boolean upgradeMessagePriority(Handler handler, MessageQueue messageQueue,
                                          TargetMessageChecker targetMessageChecker) {
        synchronized (messageQueue) {
            try {
                Message message = (Message) filed_mMessages.get(messageQueue);
                Message preMessage = null;
                while (message != null) {
                    if (targetMessageChecker.isTargetMessage(message)) {
                        // 拷贝消息
                        Message copy = copyMessage(message);
                        if (preMessage != null) { //如果已经在队列首部了，则不需要优化
                            //当前消息的下一个消息
                            Message next = nextMessage(message);
                            setMessageNext(preMessage, next);
                            handler.sendMessageAtFrontOfQueue(copy);
                            return true;
                        }
                        return false;
                    }
                    preMessage = message;
                    message = nextMessage(message);
                }
            } catch (Exception e) {
                Timber.e(e, "upgradeMessagePriority failed");
            }
        }
        return false;
    }

    private boolean removeSyncBarrier(MessageQueue messageQueue, int token) {
        try {
            Method removeSyncBarrier = HiddenReflectionUtil.getDeclaredMethod(MessageQueue.class,"removeSyncBarrier",int.class);
            removeSyncBarrier.setAccessible(true);
            removeSyncBarrier.invoke(messageQueue, token);
            return true;
        } catch (Exception e) {
            //should never happen
            Timber.e(e, "removeSyncBarrier failed");
            return false;
        }

    }

    /**
     * 移动消息屏障至队首
     *
     * @param messageQueue
     * @param handler
     * @return
     */
    public boolean upgradeBarrierMessagePriority(MessageQueue messageQueue, Handler handler) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            return false;
        }
        synchronized (messageQueue) {
            try {
                Message message = (Message) filed_mMessages.get(messageQueue);
                Message preMessage = null;
                int index = 0;

                while (message != null) {
                    if (message.getTarget() == null) {
                        if (preMessage != null) { //如果已经在队列首部了，则不需要优化
                            //当前消息的下一个消息
                            // 拷贝消息
                            Message copy = copyMessage(message);
                            boolean success = handler.sendMessageAtFrontOfQueue(copy);
                            copy.setTarget(null);
                            if (success){
                                Message next = nextMessage(message);
                                setMessageNext(preMessage, next);
                                setMessageNext(message,null);
                                log("提升了屏障消息优先级成功: ->" + message + " 提升的顺序，原来位于 " + index);
                            } else {
                                log("提升屏障优先级失败");
                            }
                            return true;
                        }else {
                            return true;
                        }
                    }
                    preMessage = message;
                    message = nextMessage(message);
                    index++;
                }


                //采用API方式移动
                //反射获取 head Message
                // Message message = (Message) filed_mMessages.get(messageQueue);
                // if (message != null && message.getTarget() == null) { //如果队首已经是异步消息，无需优化、直接返回
                //     return false;
                // }
                // int index = 0;
                // while (message != null) {
                //     if (message.getTarget() == null) { // target 为null 说明该消息为 屏障消息
                //         Message cloneBarrier = Message.obtain(message);
                //         removeSyncBarrier(messageQueue, message.arg1); //message.arg1 是屏障消息的 token, 后续的async消息会根据这个值进行屏障消息的移除
                //         handler.sendMessageAtFrontOfQueue(cloneBarrier);
                //         log("提升了屏障优先级 ->" + message + " 提升的顺序，原来位于 " + index);
                //         cloneBarrier.setTarget(null);//屏障消息的target为null，因此这里还原下
                //         return true;
                //     }
                //     message = nextMessage(message);
                //     index++;
                // }
            } catch (Exception e) {
                Log.e(TAG, "upgradeBarrierMessagePriority failed", e);
                if (debug) {
                    throw new UnsupportedOperationException(e);
                }
            }
        }
        return false;
    }

    public boolean upgradeMessagePriority(Handler handler, MessageQueue messageQueue, int what) {
        synchronized (messageQueue) {
            try {
                Message message = (Message) filed_mMessages.get(messageQueue);
                Message preMessage = null;
                int index = 0;
                while (message != null) {
                    if (message.what == what && message.getTarget() == handler) {
                        if (index == 0){
                            return true;
                        }
                        Message copy = copyMessage(message);
                        // handler.removeMessages(what);
                        Message next = nextMessage(message);
                        setMessageNext(preMessage, next);
                        handler.sendMessageAtFrontOfQueue(copy);
                        log("提升了消息优先级从 "+index+" 提升到队首");
                        return true;
                    }
                    preMessage = message;
                    message = nextMessage(message);
                    index++;
                }
            } catch (Exception e) {
                //todo report
                e.printStackTrace();
                if (debug) {
                    throw new UnsupportedOperationException(e);
                }
            }
        }

        return false;
    }

    private static Message copyMessage(Message message) {
        Message copy = Message.obtain(message);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (message.isAsynchronous()) {
                copy.setAsynchronous(true);
            }
        }
        return copy;
    }

    public Handler getCurHandlerOfViewRootImpl() {
        return handlerOfViewRoot;
    }

    public void setCurrentViewRoot(ViewParent parent) {
        try {
            if (parent == null) {
                handlerOfViewRoot = null;
                return;
            }
            if (field_mHandlerOfViewRootImpl == null) {
                class_ViewRootImpl = Class.forName("android.view.ViewRootImpl");
                field_mHandlerOfViewRootImpl = class_ViewRootImpl.getDeclaredField("mHandler");
                field_mHandlerOfViewRootImpl.setAccessible(true);
            }
            handlerOfViewRoot = (Handler) field_mHandlerOfViewRootImpl.get(parent);
        } catch (Exception e) {
            stateListener.onUnExpectError(TAG, "setCurrentViewRoot failed", e);
        }
    }

    public void printMessages() {
        printMessages(mhHandlerMessageQueue);
    }

    public void printMessages(MessageQueue messageQueue) {
        try {
            Message msg = (Message) filed_mMessages.get(messageQueue);
            StringBuilder sb = new StringBuilder();
            sb.append("messages: ");
            while (msg != null) {
                sb.append("{");
                sb.append("handler= ").append(msg.getTarget()).append(", what=").append(msg.what).append(" }");
                msg = nextMessage(msg);
            }
            log(sb.toString());

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void log(String msg) {
        if (DEBUG) {
            Timber.tag("MainLooperBoost").d(msg);
        }
    }

    public void errorLog(String msg) {
        Timber.tag("MainLooperBoost").e(msg);

    }

    public void onUnExpectError(String tag,
                                String msg,
                                Throwable e) {
        //
        this.setEnable(false);
        this.stateListener.onUnExpectError(tag, msg, e);
    }


}
