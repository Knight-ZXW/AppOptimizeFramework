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
#include <shadowhook.h>
#include <unistd.h>
#include "xdl.h"
#include "mutex"
namespace kbArt {
void *ArtHelper::runtime = nullptr;
void *ArtHelper::partialRuntime = nullptr;
char *ArtHelper::artPath = nullptr;
JniIdManager *ArtHelper::jniIdManager = nullptr;

static WalkStack_t walk_stack = nullptr;
static Resume_t resume = nullptr;
static PrettyMethod_t pretty_method = nullptr;
static SuspendThreadByPeer_t suspend_thread_by_peer = nullptr;
static SuspendThreadByPeer_Q_t suspend_thread_by_peer_Q = nullptr;
static SuspendThreadByThreadId_t suspend_thread_by_thread_id = nullptr;


static void *thread_list = nullptr;

static int api_level = 0;

static std::mutex mutex;



//static std::map<std::string, int> findSymbolRecordMap;


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
  artPath = getLibArtPath();
  auto start = std::chrono::steady_clock::now();
  void *handle = xdl_open(artPath,
                          XDL_TRY_FORCE_LOAD);
  auto end = std::chrono::steady_clock::now();
  LOGE(TAG,"open xdl cost %lld",std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count());


  suspend_thread_by_thread_id =
      reinterpret_cast<SuspendThreadByThreadId_t>(xdl_dsym(handle,
                                                           "_ZN3art10ThreadList23SuspendThreadByThreadIdEjNS_13SuspendReasonEPb",
                                                           nullptr));
  LOGV("ArtHelper", "suspend_thread_by_thread_id is %p", suspend_thread_by_thread_id);

  if (suspend_thread_by_thread_id == nullptr) {
    return -1;
  }

  if (api_level > __ANDROID_API_Q__) {
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

  xdl_close(handle);
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
        reinterpret_cast<char *>(runtime) + offsetOfVmExt -
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
    //TODO 未验证的系统版本
    return -1;
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
  if (resume == nullptr) {
    void *handle = getArtSoHandle();
    resume = reinterpret_cast<Resume_t>(xdl_dsym(handle,
                                                 "_ZN3art10ThreadList6ResumeEPNS_6ThreadENS_13SuspendReasonE",
                                                 nullptr));
    xdl_close(handle);
  }
  return resume(ArtHelper::getThreadList(), thread, suspendReason);
}

std::string ArtHelper::PrettyMethod(void *art_method, bool with_signature) {
  if (pretty_method == nullptr) {
    void *handle = getArtSoHandle();
    pretty_method = reinterpret_cast<PrettyMethod_t>(xdl_dsym(handle,
                                                              "_ZN3art9ArtMethod12PrettyMethodEb",
                                                              nullptr));
    xdl_close(handle);
  }
  return pretty_method(art_method, with_signature);
}

void ArtHelper::StackVisitorWalkStack(StackVisitor *visitor, bool include_transitions) {
  if (walk_stack == nullptr) {
    void *handle = getArtSoHandle();
    walk_stack = reinterpret_cast<WalkStack_t>(xdl_dsym(handle,
                                                        "_ZN3art12StackVisitor9WalkStackILNS0_16CountTransitionsE0EEEvb",
                                                        nullptr));
    xdl_close(handle);
  }
  walk_stack(visitor, include_transitions);
}

bool ArtHelper::SetJdwpAllowed(bool allowed) {
  static void (*setJdwpAllowed)(bool) = nullptr;
  if (setJdwpAllowed == nullptr) {
    void *handle = getArtSoHandle();

    setJdwpAllowed = reinterpret_cast<void (*)(bool)>(xdl_dsym(handle,
                                                               "_ZN3art3Dbg14SetJdwpAllowedEb",
                                                               nullptr));
    xdl_close(handle);
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
    void *handle = getArtSoHandle();
    setJavaDebuggable = reinterpret_cast<void (*)(void *, bool)>(xdl_dsym(handle,
                                                                          "_ZN3art7Runtime17SetJavaDebuggableEb",
                                                                          nullptr));
    xdl_close(handle);
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
    void *handle = getArtSoHandle();
    isJdwpAllow =
        reinterpret_cast<bool (*)()>(xdl_dsym(handle, "_ZN3art3Dbg13IsJdwpAllowedEv", nullptr));
    xdl_close(handle);
  }
  return isJdwpAllow();
}

JniIdManager *ArtHelper::getJniIdManager() {
  if (api_level < ANDROID_API_TIRAMISU) {
    return nullptr;
  }
  if (jniIdManager == nullptr) {
    void *address = nullptr;
    if (api_level >= ANDROID_API_TIRAMISU) {
      address =
          static_cast<void *>(
              (reinterpret_cast<PartialRuntimeTiramisu *>(ArtHelper::partialRuntime))->jni_id_manager_);
    } else if (api_level >= ANDROID_API_R) {
      address = static_cast<JniIdManager *>(
          (reinterpret_cast<PartialRuntimeR *>(ArtHelper::partialRuntime))->jni_id_manager_);
    }
    if (address != nullptr) {
      jniIdManager = new JniIdManager(ArtHelper::runtime, address);
    }
  }
  return jniIdManager;
}

bool ArtHelper::DisableClassVerify() {
  //_ZN3art7Runtime15DisableVerifierEv
  static void (*DisableVerifierEv)(void *) = nullptr;
  if (DisableVerifierEv == nullptr) {
    void *handle = getArtSoHandle();
    DisableVerifierEv = reinterpret_cast<void (*)(void *)>(xdl_dsym(handle,
                                                                    "_ZN3art7Runtime15DisableVerifierEv",
                                                                    nullptr));
    xdl_close(handle);
  }
  LOGE(TAG, "fun is %p", DisableVerifierEv);
  if (DisableVerifierEv != nullptr) {
    DisableVerifierEv(ArtHelper::runtime);
    return true;
  }
  return false;
}
bool ArtHelper::EnableClassVerify() {
  return false;
}

void *originJit = nullptr;
void *jitStub = nullptr;
void delayJitFunc(void *task, void *thread) {
  LOGI(TAG, " jit invoked, delay it ");
  sleep(5);
  LOGI(TAG, "jit task wake up");
  ((void (*)(void *, void *)) originJit)(task, thread);
  LOGI(TAG, "jit task done");
}

bool ArtHelper::DelayJit() {
  const char *jit = "_ZN3art3jit14JitCompileTask3RunEPNS_6ThreadE";
  jitStub = shadowhook_hook_sym_name("libart.so", jit,
                                     (void *) delayJitFunc,
                                     (void **) &originJit);
  if (jitStub != nullptr) {
    LOGI(TAG, "hook to delay Jit success");
    return true;
  } else {
    LOGE(TAG, "hook to delay Jit failed");
    return false;
  }
}
bool ArtHelper::ResumeJit() {
  if (jitStub != nullptr) {
    shadowhook_unhook(jitStub);
    return true;
  }
  return false;
}
void *ArtHelper::getArtSoHandle() {
  return   xdl_open(artPath,XDL_TRY_FORCE_LOAD);;
}

JniIdManager::JniIdManager(void *runtime, void *instanceRef) : _instanceRef(instanceRef) {
}
void *JniIdManager::DecodeMethodId(jmethodID methodId) {
  if (api_level < ANDROID_API_R) {
    return methodId;
  }
  static void *(*_decodeMethodId)(void *, jmethodID) = nullptr;
  if (_decodeMethodId == nullptr) {
    void *handle = ArtHelper::getArtSoHandle();
    _decodeMethodId = reinterpret_cast<void *(*)(void *, jmethodID)>(xdl_dsym(
        handle,
        "_ZN3art3jni12JniIdManager14DecodeMethodIdEP10_jmethodID",
        nullptr));
    xdl_close(handle);
  }
  return _decodeMethodId(_instanceRef, methodId);
}
}
