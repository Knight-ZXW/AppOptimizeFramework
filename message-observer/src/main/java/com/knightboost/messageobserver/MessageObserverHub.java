package com.knightboost.messageobserver;

import android.os.Message;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class MessageObserverHub {

    private final List<MessageObserver> mObservers = new ArrayList<>();
    private final List<MessageObserver> mAddObservers = new ArrayList<>();
    private boolean haveAdd = false;

    private final List<MessageObserver> mRemoveObservers = new ArrayList<>();
    private boolean haveRemove = false;

    public void addMessageObserver(MessageObserver messageObserver) {
        if (messageObserver == null) {
            return;
        }
        synchronized (this) {
            if (!mAddObservers.contains(messageObserver)) {
                mAddObservers.add(messageObserver);
                this.haveAdd = true;
            }
        }
    }

    public void removeMessageObserver(MessageObserver messageObserver) {
        if (messageObserver == null) {
            return;
        }
        synchronized (this) {
            if (!mRemoveObservers.contains(messageObserver)) {
                mRemoveObservers.add(messageObserver);
                this.haveRemove =true;
            }
        }
    }

    public void messageDispatchStarting(@Nullable String msg){
        if (this.haveAdd){
            synchronized (this){
                for (MessageObserver observer : this.mAddObservers) {
                    if (!this.mObservers.contains(observer)){
                        this.mObservers.add(observer);
                    }
                }
                this.mAddObservers.clear();
                this.haveAdd = false;
            }
        }
        for (MessageObserver observer : this.mObservers) {
            observer.onMessageDispatchStarting(msg);
        }

    }


    public void messageDispatched(@Nullable String msg,@Nullable Message message){
        for (MessageObserver observer : this.mObservers) {
            observer.onMessageDispatched(msg,message);
        }
        if (this.haveRemove){
            synchronized (this){
                for (MessageObserver removingObserver : mRemoveObservers) {
                    this.mObservers.remove(removingObserver);
                    this.mAddObservers.remove(removingObserver);
                }
                this.mRemoveObservers.clear();
                this.haveRemove = false;
            }

        }
    }

}
