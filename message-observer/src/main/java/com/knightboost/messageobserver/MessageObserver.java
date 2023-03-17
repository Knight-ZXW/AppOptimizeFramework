package com.knightboost.messageobserver;

import android.os.Message;

import androidx.annotation.Nullable;

public interface MessageObserver {
    /**
     * msg 是否有值依赖于当前设备版本
     * @param msg
     */
    void onMessageDispatchStarting(String msg);

    /**
     *
     * @param msg  msg
     * @param message message
     */
    void onMessageDispatched(String msg,@Nullable Message message);

}
