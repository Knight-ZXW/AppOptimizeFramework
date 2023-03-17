//
// Created by Administrator on 2022/12/28.
// Email: nimdanoob@163.com
//

#include "art.h"
#include "stackvisitor.h"

#include "logger.h"
#include <vector>
#include <string>
#include <cstdlib>
#include <sys/system_properties.h>
#include "xdl.h"
#include "mutex"

namespace kbArt {
void *ArtHelper::runtime = nullptr;
void *ArtHelper::partialRuntime = nullptr;
void *ArtHelper::artHandle = nullptr;
JniIdManager *ArtHelper::jniIdManager = nullptr;

static WalkStack_t walk_stack = nullptr;
static SuspendThreadByPeer_t suspend_thread_by_peer = nullptr;
static SuspendThreadByPeer_Q_t suspend_thread_by_peer_Q = nullptr;

static SuspendThreadByThreadId_t suspend_thread_by_thread_id = nullptr;

static Resume_t resume = nullptr;

static PrettyMethod_t pretty_method = nullptr;
static FetchState_t fetchState = nullptr;
static GetCpuMicroTime_t getCpuMicroTime = nullptr;

static void (*pSetJdwpAllowed)(bool) = nullptr;

static void *thread_list = nullptr;

static int api_level = 0;

#define ANDROID_API_P 28
#define ANDROID_API_Q 29
#define ANDROID_API_R 30
#define ANDROID_API_S 31
#define ANDROID_API_TIRAMISU 33
#define TAG "ArtHelper"

//source from: https://github.com/tiann/FreeReflection/blob/master/library/src/main/cpp/art.cpp
template<typename T>
int findOffset(void *start, int regionStart, int regionEnd, T target) {
  if (nullptr == start || regionStart < 0 || regionEnd <= 0) {
    return -1;
  }
  char *c_start = reinterpret_cast<char *>(start);
  for (int i = regionStart; i < regionEnd; i += 4) {
    T *current_value = reinterpret_cast<T *>(c_start + i);
    if (target == *current_value) {
      LOGV("artHelper", "find target success, address is %p", current_value);
      return i;
    }
  }
  return -1;
}

/**
 * 获取主要符号
 * @return
 */
int ArtHelper::load_symbols() {
  LOGV("ArtHelper", "start load art symbols");
  api_level = getAndroidApiLevel();
  const char *artPath = getLibArtPath();
  void *handle = xdl_open(artPath,
                          XDL_TRY_FORCE_LOAD);
  ArtHelper::artHandle = handle;
  LOGV("ArtHelper", "handle is %p", handle);

  walk_stack = reinterpret_cast<WalkStack_t>(xdl_dsym(handle,
                                                      "_ZN3art12StackVisitor9WalkStackILNS0_16CountTransitionsE0EEEvb",
                                                      nullptr));
  LOGV("ArtHelper", "walk_stack is %p", walk_stack);

  if (walk_stack == nullptr) {
    return -1;
  }

  suspend_thread_by_thread_id =
      reinterpret_cast<SuspendThreadByThreadId_t>(xdl_dsym(handle,
                                                           "_ZN3art10ThreadList23SuspendThreadByThreadIdEjNS_13SuspendReasonEPb",
                                                           nullptr));
  LOGV("ArtHelper", "suspend_thread_by_thread_id is %p", suspend_thread_by_thread_id);

  if (suspend_thread_by_thread_id == nullptr) {
    return -1;
  }

  if (api_level > __ANDROID_API_Q__) { //TODO Android 11未支持
    suspend_thread_by_peer =
        reinterpret_cast<SuspendThreadByPeer_t>(xdl_dsym(handle,
                                                         "_ZN3art10ThreadList19SuspendThreadByPeerEP8_jobjectNS_13SuspendReasonEPb",
                                                         nullptr));
    if (suspend_thread_by_peer == nullptr) {
      //todo handle
    }
  } else {
    suspend_thread_by_peer_Q =
        reinterpret_cast<SuspendThreadByPeer_Q_t>(xdl_dsym(handle,
                                                           "_ZN3art10ThreadList19SuspendThreadByPeerEP8_jobjectbNS_13SuspendReasonEPb",
                                                           nullptr));
    if (suspend_thread_by_peer_Q == nullptr) {
      //todo handle
    }
  }

  resume = reinterpret_cast<Resume_t>(xdl_dsym(handle,
                                               "_ZN3art10ThreadList6ResumeEPNS_6ThreadENS_13SuspendReasonE",
                                               nullptr));
  if (resume == nullptr) {
    return -1;
  }

  pretty_method = reinterpret_cast<PrettyMethod_t>(xdl_dsym(handle,
                                                            "_ZN3art9ArtMethod12PrettyMethodEb",
                                                            nullptr));
  if (pretty_method == nullptr) {
    return -1;
  }

  getCpuMicroTime =
      reinterpret_cast<GetCpuMicroTime_t>(xdl_dsym(handle, "_ZNK3art6Thread15GetCpuMicroTimeEv",
                                                   nullptr));

  return 0;
}
int ArtHelper::init(JNIEnv *env) {
  api_level = getAndroidApiLevel();
  JavaVM *javaVM;
  env->GetJavaVM(&javaVM);

  auto *javaVMExt = reinterpret_cast<JavaVMExt *>(javaVM);
  runtime = javaVMExt->runtime;
  const int MAX = 2000;

  int loadSymbolResult = load_symbols();
  if (loadSymbolResult == -1) {
    LOGE("ArtHelper", "loadSymbol failed");
    return -1;
  }

  int offsetOfVmExt = findOffset(runtime, 0, MAX, javaVMExt);
  if (offsetOfVmExt < 0) {
    LOGV("ArtHelper", "find offset of VmExt failed: %d", offsetOfVmExt);
    return -1;
  }

  if (api_level >= ANDROID_API_TIRAMISU) {
    ArtHelper::partialRuntime =
        reinterpret_cast<char *>(runtime) + offsetOfVmExt -
            offsetof(PartialRuntimeTiramisu, java_vm_);
    thread_list =
        reinterpret_cast<PartialRuntimeTiramisu *>(ArtHelper::partialRuntime)->thread_list_;
  } else if (api_level >= ANDROID_API_R) {
    ArtHelper::partialRuntime =
        reinterpret_cast<char *>(partialRuntime) + offsetOfVmExt -
            offsetof(PartialRuntimeR, java_vm_);
    thread_list = reinterpret_cast<PartialRuntimeR *>(ArtHelper::partialRuntime)->thread_list_;
  } else if (api_level >= ANDROID_API_Q) {
    ArtHelper::partialRuntime =
        reinterpret_cast<char *>(runtime) + offsetOfVmExt -
            offsetof(PartialRuntimeQ, java_vm_);
    thread_list = reinterpret_cast<PartialRuntimeQ *>(ArtHelper::partialRuntime)->thread_list_;
  } else if (api_level >= __ANDROID_API_O_MR1__) {
    ArtHelper::partialRuntime =
        reinterpret_cast<char *>(runtime) + offsetOfVmExt -
            offsetof(PartialRuntimeP, java_vm_);
    thread_list = reinterpret_cast<PartialRuntimeP *>(ArtHelper::partialRuntime)->thread_list_;
  } else {
    //TODO
  }

  return 0;
}

void *ArtHelper::getThreadList() {
  return thread_list;
}

void *
ArtHelper::suspendThreadByPeer(jobject peer, SuspendReason suspendReason, bool *timed_out) {
  return suspend_thread_by_peer(ArtHelper::getThreadList(), peer, suspendReason, timed_out);
}

void *ArtHelper::SuspendThreadByThreadId(uint32_t threadId,
                                         SuspendReason suspendReason,
                                         bool *timed_out) {
  return suspend_thread_by_thread_id(thread_list, threadId, suspendReason, timed_out);
}

bool ArtHelper::Resume(void *thread, SuspendReason suspendReason) {
  return resume(ArtHelper::getThreadList(), thread, suspendReason);
}

std::string ArtHelper::PrettyMethod(void *art_method, bool with_signature) {
  return pretty_method(art_method, with_signature);
}

void ArtHelper::StackVisitorWalkStack(StackVisitor *visitor, bool include_transitions) {
  walk_stack(visitor, include_transitions);
}
//todo
ThreadState ArtHelper::FetchState(void *thread, void *monitor_object, uint32_t *lock_owner_tid) {
  return fetchState(thread, monitor_object, lock_owner_tid);
}

// Returns the thread-specific CPU-time clock in microseconds or -1 if unavailable.
uint64_t ArtHelper::GetCpuMicroTime(void *thread) {
  return getCpuMicroTime(thread);
}

bool ArtHelper::SetJdwpAllowed(bool allowed) {
  static void (*setJdwpAllowed)(bool) = nullptr;
  if (setJdwpAllowed == nullptr) {
    setJdwpAllowed = reinterpret_cast<void (*)(bool)>(xdl_dsym(artHandle,
                                                               "_ZN3art3Dbg14SetJdwpAllowedEb",
                                                               nullptr));
  }
  if (setJdwpAllowed != nullptr) {
    setJdwpAllowed(allowed);
    return true;
  } else {
    return false;
  }
}

bool ArtHelper::SetJavaDebuggable(bool debuggable) {

  static void (*setJavaDebuggable)(void *, bool) = nullptr;
  if (setJavaDebuggable == nullptr) {
    setJavaDebuggable = reinterpret_cast<void (*)(void *, bool)>(xdl_dsym(artHandle, "_ZN3art7Runtime17SetJavaDebuggableEb", nullptr));
  }
  if (setJavaDebuggable != nullptr) {
    setJavaDebuggable(runtime, debuggable);
    return true;
  }
  return false;
}
bool ArtHelper::IsJdwpAllow() {
  static bool (*isJdwpAllow)() = nullptr;
  if (isJdwpAllow == nullptr) {
    isJdwpAllow = reinterpret_cast<bool (*)()>(xdl_dsym(artHandle, "_ZN3art3Dbg13IsJdwpAllowedEv", nullptr));
  }
  return isJdwpAllow();
}
void *ArtHelper::findArtDsym(const char *symbol, size_t *symbol_size) {
  return xdl_dsym(artHandle,
                  symbol,
                  symbol_size);
}
JniIdManager *ArtHelper::getJniIdManager() {
  if (api_level < ANDROID_API_TIRAMISU) {
    return nullptr;
  }
  if (jniIdManager == nullptr) {
    void * address = nullptr;
    if (api_level >= ANDROID_API_TIRAMISU) {
       address =
          static_cast<void *>(
              (reinterpret_cast<PartialRuntimeTiramisu *>(ArtHelper::partialRuntime))->jni_id_manager_);
    } else if (api_level >= ANDROID_API_R) {
      address = static_cast<JniIdManager *>(
          (reinterpret_cast<PartialRuntimeR *>(ArtHelper::partialRuntime))->jni_id_manager_);
    }
    if (address!= nullptr){
      jniIdManager = new JniIdManager(ArtHelper::runtime,address);
    }
  }
  return jniIdManager;
}



JniIdManager::JniIdManager(void *runtime, void *instanceRef):_instanceRef(instanceRef) {
}
void *JniIdManager::DecodeMethodId(jmethodID methodId) {
  if (api_level<ANDROID_API_R){
    return methodId;
  }
  static void* (*_decodeMethodId)(void*, jmethodID) = nullptr;
  if (_decodeMethodId == nullptr){
    _decodeMethodId = reinterpret_cast<void *(*)(void*, jmethodID)>(ArtHelper::findArtDsym("_ZN3art3jni12JniIdManager14DecodeMethodIdEP10_jmethodID", nullptr));
  }
  return _decodeMethodId(_instanceRef, methodId);
}
}
