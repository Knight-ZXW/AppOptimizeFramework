package com.knightboost.looper.free;
import android.os.Message;

import androidx.annotation.Keep;

@Keep
public interface LooperMessageObserver {
    /**
     * Called right before a message is dispatched.
     *
     * <p> The token type is not specified to allow the implementation to specify its own type.
     *
     * @param token Token obtained by previously calling
     *              {@link Looper.Observer#messageDispatchStarting} on the same Observer instanceRef.
     * @return a token used for collecting telemetry when dispatching a single message.
     *         The token token must be passed back exactly once to either
     *         {@link Looper.Observer#messageDispatched} or {@link Looper.Observer#dispatchingThrewException}
     *         and must not be reused again.
     *
     */
    Object messageDispatchStarting(Object token);

    /**
     * Called when a message was processed by a Handler.
     *
     * @param token Token obtained by previously calling
     *              {@link Looper.Observer#messageDispatchStarting} on the same Observer instanceRef.
     * @param msg The message that was dispatched.
     */
    void messageDispatched(Object token, Message msg);

    /**
     * Called when an exception was thrown while processing a message.
     *
     * @param token Token obtained by previously calling
     *              {@link Looper.Observer#messageDispatchStarting} on the same Observer instanceRef.
     * @param msg The message that was dispatched and caused an exception.
     * @param exception The exception that was thrown.
     */
    void dispatchingThrewException(Object token, Message msg, Exception exception);
}
