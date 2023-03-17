package com.knightboost.optimize.looperopt;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import com.knightboost.messageobserver.MessageObserver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import timber.log.Timber;

import static com.knightboost.optimize.looperopt.MsgCode.VIEW_ROOT_HANDLER_MSG_WINDOW_FOCUS_CHANGED;

public class MessageOptHandler implements MessageObserver {

    private final LooperMsgOptimizeManager mainLooperBoost;
    private Method method_getTargetState;
    private Field field_mLifecycleStateRequest;

    private boolean pauseActivityUpgradeSuccess;
    private boolean startActivityUpgradeSuccess;

    private static final int TARGET_STATE_ON_PAUSE = 4;
    private static final int TARGET_STATE_ON_RESUME = 3;

    private static final String TAG = "MessageOptHandler";

    @SuppressLint("SoonBlockedPrivateApi")
    public MessageOptHandler(LooperMsgOptimizeManager mainLooperBoost) {
        this.mainLooperBoost = mainLooperBoost;
        try {
            Class<?> class_ClientTransaction = Class.forName("android.app.servertransaction.ClientTransaction");
            field_mLifecycleStateRequest = HiddenReflectionUtil.getDeclaredField(class_ClientTransaction, "mLifecycleStateRequest");
            field_mLifecycleStateRequest.setAccessible(true);
            Class<?> class_ActivityLifecycleItem = Class.forName("android.app.servertransaction.ActivityLifecycleItem");
            method_getTargetState = HiddenReflectionUtil.getDeclaredMethod(class_ActivityLifecycleItem, "getTargetState");
            method_getTargetState.setAccessible(true);
        } catch (Exception e) {
            Timber.e(e, "initiate failed");
            mainLooperBoost.onUnExpectError(TAG, "initiate failed", e);
        }

    }

    @Override
    public void onMessageDispatchStarting(String msg) {

    }

    /**
     * 当前消息处理结束回调
     *
     * @param msg     msg
     * @param message message
     */
    @Override
    public void onMessageDispatched(String msg, @Nullable Message message) {
        //进行消息队列检查
        int type = mainLooperBoost.getOptimizeType();
        try {
            switch (type) {
                //1. 检查 startActivity相应消息
                case MsgOptimizeType.TYPE_OPTIMIZE_NEXT_START_ACTIVITY_MSG:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (!pauseActivityUpgradeSuccess) {
                            mainLooperBoost.log("检查pause消息");
                            pauseActivityUpgradeSuccess = mainLooperBoost.upgradeMessagePriority(mainLooperBoost.getmH(),
                                    mainLooperBoost.getMessageQueue(), isPauseMessage);
                            if (pauseActivityUpgradeSuccess) {
                                mainLooperBoost.log("!!! optimize toPauseActivity  success");
                            }
                        }

                        if (!startActivityUpgradeSuccess) {
                            mainLooperBoost.log("检查start消息");

                            startActivityUpgradeSuccess = mainLooperBoost.upgradeMessagePriority(mainLooperBoost.getmH(),
                                    mainLooperBoost.getMessageQueue(), isResumeActivityMessage);
                            if (startActivityUpgradeSuccess) {
                                mainLooperBoost.clearState();
                                mainLooperBoost.log("!!! optimize toResumeActivity  success");
                            }
                        }
                    } else {
                        if (!pauseActivityUpgradeSuccess) {
                            pauseActivityUpgradeSuccess = mainLooperBoost.upgradeMessagePriority(mainLooperBoost.getmH(),
                                    mainLooperBoost.getMessageQueue(), MsgCode.MH_MSG_PAUSE_ACTIVITY);
                            if (pauseActivityUpgradeSuccess) {
                                mainLooperBoost.log("!!! optimize toPauseActivity Success");
                                //log pause_activity opt success
                            }
                        }
                        boolean startActivityOptSuccess = mainLooperBoost.upgradeMessagePriority(mainLooperBoost.getmH(),
                                mainLooperBoost.getMessageQueue(), MsgCode.MH_MSG_LAUNCH_ACTIVITY);

                        if (!startActivityOptSuccess) return;
                        mainLooperBoost.clearState();
                        mainLooperBoost.log("!!! optimize toLaunchActivity Success");
                    }
                    break;
                //2. 帧绘制消息优化
                case MsgOptimizeType.TYPE_OPTIMIZE_NEXT_DO_FRAME:
                    boolean success = false;
                    if (mainLooperBoost.isUpgradeVsyncByBarrier()) {
                        mainLooperBoost.log("使用Barrier 方案提升帧绘制");
                        success = mainLooperBoost.upgradeBarrierMessagePriority(
                                mainLooperBoost.getMessageQueue(), mainLooperBoost.getChoreographerHandler()
                        );
                    } else {
                        mainLooperBoost.log("使用 非Barrier 方案提升帧绘制");
                        success = mainLooperBoost.upgradeMessagePriority(
                                mainLooperBoost.getChoreographerHandler()
                                , mainLooperBoost.getMessageQueue(), 0
                        );
                    }

                    if (success) {
                        mainLooperBoost.log("!!! optimize DO_FRAME Success");
                        mainLooperBoost.clearState();
                    }
                    break;
                case MsgOptimizeType.TYPE_OPTIMIZE_WINDOW_FOCUS_CHANGE:
                    Handler handler = mainLooperBoost.getCurHandlerOfViewRootImpl();
                    if (handler == null) { //failed
                        mainLooperBoost.clearState();
                        return;
                    }
                    boolean upgradeSuccess = mainLooperBoost.upgradeMessagePriority(handler,
                            mainLooperBoost.getMessageQueue(), VIEW_ROOT_HANDLER_MSG_WINDOW_FOCUS_CHANGED);
                    if (!upgradeSuccess) {
                        return;
                    }
                    mainLooperBoost.clearState();
                    mainLooperBoost.log("!! optimize view root window focus change  success");
                    // log msg_window_focus_changed opt success
                    //put record
                    break;
                default:
                    break;

            }

        } catch (Exception e) {
            //reset opt state
            mainLooperBoost.clearState();
            LooperMsgOptimizeManager.getInstance()
                    .onUnExpectError(TAG,
                            "handle msg failed", e);
        }
    }

    private final TargetMessageChecker isPauseMessage = new TargetMessageChecker() {
        @Override
        public boolean isTargetMessage(Message message) {
            if (message.what != 159) {
                return false;
            }
            Object obj = message.obj;
            if (obj == null) {
                return false;
            }
            try {
                Object lifecycleStateRequest = field_mLifecycleStateRequest.get(obj);
                if (lifecycleStateRequest == null) {
                    return false;
                }
                Integer targetState = (Integer) method_getTargetState.invoke(lifecycleStateRequest);
                if (targetState != null && targetState == TARGET_STATE_ON_PAUSE) {
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
            return false;
        }
    };

    private final TargetMessageChecker isResumeActivityMessage = new TargetMessageChecker() {
        @Override
        public boolean isTargetMessage(Message message) {
            if (message.what != 159) {
                return false;
            }
            Object obj = message.obj;
            if (obj == null) {
                return false;
            }
            try {
                Object lifecycleStateRequest = field_mLifecycleStateRequest.get(obj);
                if (lifecycleStateRequest == null) {
                    return false;
                }
                Integer targetState = (Integer) method_getTargetState.invoke(lifecycleStateRequest);
                if (targetState != null && targetState == TARGET_STATE_ON_RESUME) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }
    };

}
