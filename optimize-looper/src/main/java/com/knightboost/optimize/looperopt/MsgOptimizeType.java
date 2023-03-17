package com.knightboost.optimize.looperopt;

public class MsgOptimizeType {

    public static final int TYPE_NONE = 0;

    /**
     * 当前状态为 支持监听并优化下一次 启动页面消息
     */
    public static final int TYPE_OPTIMIZE_NEXT_START_ACTIVITY_MSG = 1;

    /**
     * 优化下一帧展示
     */
    public static final int TYPE_OPTIMIZE_NEXT_DO_FRAME = 2;

    /**
     * Unfinished
     */
    public static final int TYPE_OPTIMIZE_WINDOW_FOCUS_CHANGE = 3;
}
