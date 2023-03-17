package com.knightboost.optimize.looperopt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface StateListener {
    public void onUnExpectError(@NonNull String tag,
                                @NonNull String msg,
                                @Nullable Throwable e);
}
