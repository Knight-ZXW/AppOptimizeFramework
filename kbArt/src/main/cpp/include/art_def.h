//
// Created by Knight-ZXW on 2023/6/8.
//

#pragma once
namespace kbArt {

#define ANDROID_API_M 23 // Android 6.0
#define ANDROID_API_N 24 // Android 7.0
#define ANDROID_API_N_MR1 25 // Android 7.1
#define ANDROID_API_O 26 // Android 8.1
#define ANDROID_API_O_MR1 27 // Android 8.1
#define ANDROID_API_P 28 // Android 9
#define ANDROID_API_Q 29 // Android 10
#define ANDROID_API_R 30 // Android 11
#define ANDROID_API_S 31 // Android 12
#define ANDROID_API_S_V2 32 // Android 12
#define ANDROID_API_TIRAMISU 33

enum class SuspendReason : char {
  // Suspending for internal reasons (e.g. GC, stack trace, etc.).
  kInternal,
  // Suspending due to non-runtime, user controlled, code. (For example Thread#Suspend()).
  kForUserCode,
};

enum class ThreadState : uint8_t {
  // `kRunnable` was previously 67 but it is now set to 0 so that we do not need to extract
  // flags from the thread's `state_and_flags` to check for any flag being set while Runnable.
  // Note: All atomic accesses for a location should use the same data size,
  // so the incorrect old approach of reading just 16 bits has been rewritten.

  kTerminated =
  66,                 // TERMINATED     TS_ZOMBIE    Thread.run has returned, but Thread* still around
  kRunnable = 0,                    // RUNNABLE       TS_RUNNING   runnable
  kObsoleteRunnable = 67,           // ---            ---          obsolete value
  kTimedWaiting = 68,               // TIMED_WAITING  TS_WAIT      in Object.wait() with a timeout
  kSleeping,                        // TIMED_WAITING  TS_SLEEPING  in Thread.sleep()
  kBlocked,                         // BLOCKED        TS_MONITOR   blocked on a monitor
  kWaiting,                         // WAITING        TS_WAIT      in Object.wait()
  kWaitingForLockInflation,         // WAITING        TS_WAIT      blocked inflating a thin-lock
  kWaitingForTaskProcessor,         // WAITING        TS_WAIT      blocked waiting for taskProcessor
  kWaitingForGcToComplete,          // WAITING        TS_WAIT      blocked waiting for GC
  kWaitingForCheckPointsToRun,      // WAITING        TS_WAIT      GC waiting for checkpoints to run
  kWaitingPerformingGc,             // WAITING        TS_WAIT      performing GC
  kWaitingForDebuggerSend,          // WAITING        TS_WAIT      blocked waiting for events to be sent
  kWaitingForDebuggerToAttach,      // WAITING        TS_WAIT      blocked waiting for debugger to attach
  kWaitingInMainDebuggerLoop,       // WAITING        TS_WAIT      blocking/reading/processing debugger events
  kWaitingForDebuggerSuspension,    // WAITING        TS_WAIT      waiting for debugger suspend all
  kWaitingForJniOnLoad,             // WAITING        TS_WAIT      waiting for execution of dlopen and JNI on load code
  kWaitingForSignalCatcherOutput,   // WAITING        TS_WAIT      waiting for signal catcher IO to complete
  kWaitingInMainSignalCatcherLoop,  // WAITING        TS_WAIT      blocking/reading/processing signals
  kWaitingForDeoptimization,        // WAITING        TS_WAIT      waiting for deoptimization suspend all
  kWaitingForMethodTracingStart,    // WAITING        TS_WAIT      waiting for method tracing to start
  kWaitingForVisitObjects,          // WAITING        TS_WAIT      waiting for visiting objects
  kWaitingForGetObjectsAllocated,   // WAITING        TS_WAIT      waiting for getting the number of allocated objects
  kWaitingWeakGcRootRead,           // WAITING        TS_WAIT      waiting on the GC to read a weak root
  kWaitingForGcThreadFlip,          // WAITING        TS_WAIT      waiting on the GC thread flip (CC collector) to finish
  kNativeForAbort,                  // WAITING        TS_WAIT      checking other threads are not run on abort.
  kStarting,                        // NEW            TS_WAIT      native thread started, not yet ready to run managed code
  kNative,                          // RUNNABLE       TS_RUNNING   running in a JNI native method
  kSuspended,                       // RUNNABLE       TS_RUNNING   suspended by GC or debugger
};

//Android 9
struct PartialRuntimeP {
  void *thread_list_;

  void *intern_table_;

  void *class_linker_;

  void *signal_catcher_;
  // If true, the runtime will connect to tombstoned via a socket to
  // request an open file descriptor to write its traces to.
  bool use_tombstoned_traces_;

  // Location to which traces must be written on SIGQUIT. Only used if
  // tombstoned_traces_ == false.
  std::string stack_trace_file_;

  void *java_vm_;

};

//Android 10
struct PartialRuntimeQ {
  void *thread_list_;

  void *intern_table_;

  void *class_linker_;

  void *signal_catcher_;

  void *java_vm_;
};

//Android 11
//https://cs.android.com/android/platform/superproject/+/android-11.0.0_r40:art/runtime/runtime.h
struct PartialRuntimeR {
  void *thread_list_;

  void *intern_table_;

  void *class_linker_;

  void *signal_catcher_;

  void *jni_id_manager_;

  void *java_vm_;

};

//Android 13
struct PartialRuntimeTiramisu {
  void *thread_list_;

  void *intern_table_;

  void *class_linker_;

  void *signal_catcher_;

  void *small_lrt_allocator_;

  void *jni_id_manager_;

  void *java_vm_;

};

struct JavaVMExt {
  void *functions;
  void *runtime;
};






typedef void (*WalkStack_t)(StackVisitor *stack_visitor, bool include_transitions);

typedef void *(*SuspendThreadByPeer_t)(void *thread_list, jobject peer, SuspendReason suspendReason,
                                       bool *timed_out);

typedef void *(*SuspendThreadByPeer_Q_t)(void *thread_list, jobject peer,bool request_suspension,SuspendReason suspendReason,
                                         bool *timed_out);

//_ZN3art10ThreadList23SuspendThreadByThreadIdEjNS_13SuspendReasonEPb
typedef void *(*SuspendThreadByThreadId_t)(void *thread_list,
                                           uint32_t thread_id,
                                           SuspendReason suspendReason,
                                           bool *time_out);

typedef bool (*Resume_t)(void *thread_list, void *thread, SuspendReason suspendReason);

typedef std::string (*PrettyMethod_t)(void *art_method, bool with_signature);

typedef ThreadState (*FetchState_t)(void *thread,/* out */void* monitor_object,/* out */uint32_t* lock_owner_tid);


}