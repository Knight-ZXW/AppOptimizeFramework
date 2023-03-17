package com.knightboost.messageobserver

import android.os.*
import android.util.Printer
import com.knightboost.looper.free.LooperMessageObserver
import com.knightboost.looper.free.LooperObserverUtil

class MessageObserverManager {

    private val looper: Looper

    private val messageObserverHub: MessageObserverHub

    public constructor(looper: Looper, usePrinter: Boolean) {
        this.looper = looper
        this.messageObserverHub = MessageObserverHub()
        if (usePrinter) {
            setPrinter()
        } else {
            if (Build.VERSION.SDK_INT >= 29) {
                try {
                    val success = LooperObserverUtil.setObserver(object : LooperMessageObserver {
                        override fun messageDispatchStarting(token: Any?): Any? {
                            if (Thread.currentThread() == looper.thread) {
                                messageObserverHub.messageDispatchStarting(
                                    (">>>>> Dispatching to null null: 0")
                                )
                            }
                            return token
                        }

                        override fun messageDispatched(token: Any?, msg: Message?) {
                            if (Thread.currentThread() == looper.thread) {
                                messageObserverHub.messageDispatched("<<<<< Finished to Handler (android.os.FakeHandler) {000000} null", msg)
                            }
                        }

                        override fun dispatchingThrewException(token: Any?, msg: Message?, exception: Exception?) {
                        }
                    })
                    if (!success) {
                        setPrinter()
                    }
                } catch (e: Exception) {
                    //
                    e.printStackTrace()
                    setPrinter()
                }

            } else {
                setPrinter()
            }
        }

    }

    public constructor(looper: Looper) {
        this.looper = looper
        this.messageObserverHub = MessageObserverHub()
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                val success = LooperObserverUtil.setObserver(object : LooperMessageObserver {
                    override fun messageDispatchStarting(token: Any?): Any? {
                        if (Thread.currentThread() == looper.thread) {
                            messageObserverHub.messageDispatchStarting(
                                (">>>>> Dispatching to null null: 0")
                            )
                        }
                        return token
                    }

                    override fun messageDispatched(token: Any?, msg: Message?) {
                        if (Thread.currentThread() == looper.thread) {
                            messageObserverHub.messageDispatched("<<<<< Finished to Handler (android.os.FakeHandler) {000000} null", msg)
                        }
                    }

                    override fun dispatchingThrewException(token: Any?, msg: Message?, exception: Exception?) {
                    }
                })
                if (!success) {
                    setPrinter()
                }
            } catch (e: Exception) {
                //
                e.printStackTrace()
                setPrinter()
            }

        } else {
            setPrinter()
        }
    }

    companion object {
        private var isReflectToGetLoggingError = false

        private  val mainMessageObserverManager:MessageObserverManager by lazy {
            return@lazy MessageObserverManager(Looper.getMainLooper(),usePrinter)
        }

        private var usePrinter =false
        @JvmStatic
        fun getMain(): MessageObserverManager {
            return mainMessageObserverManager
        }

        @JvmStatic
        fun setUsePrinterToWatch(usePrinter: Boolean){
            this.usePrinter = usePrinter
        }

    }

    private fun setPrinter() {
        setPrinter(object : Printer {
            override fun println(x: String?) {
                val message = x ?: return
                if (message[0] == '>') {
                    messageObserverHub.messageDispatchStarting(x)
                } else if (message[1] == '<') {
                    messageObserverHub.messageDispatched(x, null)
                }

            }
        })
    }

    private fun setPrinter(printer: Printer) {
        var originalPrinter: Printer? = null;
        try {
            if (!isReflectToGetLoggingError) {
                val loggingField = Looper::class.java.getDeclaredField("mLogging")
                loggingField.isAccessible = true
                originalPrinter = loggingField.get(looper) as Printer?
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            isReflectToGetLoggingError = true
        }
        looper.setMessageLogging { x ->
            if (originalPrinter != null) {
                val oldPrinter = originalPrinter
                oldPrinter.println(x)
            }
            printer.println(x)
        }
    }

    public fun addMessageObserver(messageObserver: MessageObserver) {
        messageObserverHub.addMessageObserver(messageObserver)
    }

    public fun removeMessageObserver(messageObserver: MessageObserver) {
        messageObserverHub.removeMessageObserver(messageObserver)
    }
}