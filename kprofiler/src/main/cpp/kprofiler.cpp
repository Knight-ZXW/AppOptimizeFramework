#include "kprofiler.h"
#include "logger.h"
#include "string"
#include "jvmagent/jvmti_helper.h"
#include "jvmti.h"
#include "art.h"
#include "art_thread.h"
#include "atrace/trace/trace.h"
#include <iostream>
#include <thread>
#include <atomic>
using namespace kbArt;
//https://github.com/tiann/epic/blob/master/library/src/main/cpp/epic.cpp

static thread_local std::atomic<int> *counter = nullptr;
static uint32_t  cc=0;
static void profilerJvmtiEventMethodEntry
    (jvmtiEnv *jvmti_env,
     JNIEnv *jni_env,
     jthread thread,
     jmethodID method) {
  const uint32_t tid = Thread::Current()->GetThreadId();
  if (tid != 1) {
    return;
  }

  if (counter == nullptr){
    counter = new std::atomic<int>(0);
  }
  cc++;
  if (cc >10){
    return;
  }
  void *artMethod = ArtHelper::getJniIdManager()->DecodeMethodId(method);
  const std::string
      &pretty_method = kbArt::ArtHelper::PrettyMethod(artMethod, true);
  LOGE("KProfiler", "%d >>> (%d) %s",tid,cc, pretty_method.c_str());

//  static jmethodID getNameMethodId;
//  char *name;
//  char *sig;
//  char *gen;
//  jclass declaringClass;
//  jvmti_env->GetMethodDeclaringClass(method,&declaringClass);
////  jni_env->CallObjectMethod(declaringClass,)
//  jclass class_ref;
//  jvmti_env->GetMethodDeclaringClass(method, &class_ref);
//  char* class_name;
//  jvmti_env->GetClassSignature(class_ref, &class_name, NULL);
//  jvmti_env->GetMethodName(method, &name, &sig, &gen);
//  LOGE("KProfiler","methodName is %s ,className is %s",name,class_name);

}

static void profilerJvmtiEventMethodExit
    (jvmtiEnv *jvmti_env,
     JNIEnv *jni_env,
     jthread thread,
     jmethodID method,
     jboolean was_popped_by_exception,
     jvalue return_value) {
  const uint32_t tid = Thread::Current()->GetThreadId();
  if (tid != 1) {
    return;
  }
  if (cc == 0) {
    return;
  }
  if (cc >10){
    cc--;
    return;
  }

  void *artMethod = ArtHelper::getJniIdManager()->DecodeMethodId(method);
  const std::string
      &pretty_method = kbArt::ArtHelper::PrettyMethod(artMethod, true);

  LOGE("KProfiler", "%d <<< (%d) %s",tid, cc,pretty_method.c_str());
  cc =cc-1;

}

KProfiler *KProfiler::instance = nullptr;

KProfiler::KProfiler(jvmtiEnv *jvmti_env) : jvmti_env_(jvmti_env) {
  auto *callbacks = new jvmtiEventCallbacks;
  memset(callbacks, 0, sizeof(*callbacks));
  jvmti_event_callbacks_ = callbacks;
  int error = jvmti_env->SetEventCallbacks(callbacks, sizeof(*callbacks));
  if (error != JVMTI_ERROR_NONE) {
    LOGE("KProfiler", "error on Kprofiler setEventCallbacks");
  }
  jvmtiAgent::SetStandardCapabilities(jvmti_env);

}

void KProfiler::AddMethodEventListener(MethodEventListener *listener) {
  //TODO lock
  methodEventsListeners.push_back(listener);
  LOGE("KProfiler", "开始监控函数");
  if (methodEventsListeners.size() == 1) {
    jvmti_event_callbacks_->MethodEntry = profilerJvmtiEventMethodEntry;
    jvmti_event_callbacks_->MethodExit = profilerJvmtiEventMethodExit;
    int error = jvmti_env_->SetEventCallbacks(jvmti_event_callbacks_, sizeof(*jvmti_event_callbacks_));
    LOGE("KProfiler", "添加Listener %d", error);
    jvmtiError error1 = jvmti_env_->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY, nullptr);
    LOGE("KProfiler", "设置notification mode %d", error1);
    jvmtiError error2 = jvmti_env_->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_EXIT, nullptr);
    LOGE("KProfiler", "设置notification mode2 %d", error2);

  }
}

void KProfiler::RemoveMethodEventListener(MethodEventListener *listener) {
  for (auto it = methodEventsListeners.begin(); it != methodEventsListeners.end(); ++it) {
    if (*it == listener) {
      methodEventsListeners.erase(it);
      break;
    }
  }
  if (methodEventsListeners.empty()){
    jvmti_env_->SetEventNotificationMode(JVMTI_DISABLE,JVMTI_EVENT_METHOD_ENTRY, nullptr);
    jvmti_env_->SetEventNotificationMode(JVMTI_DISABLE,JVMTI_EVENT_METHOD_EXIT, nullptr);
    jvmti_event_callbacks_->MethodEntry= nullptr;
    jvmti_event_callbacks_->MethodExit = nullptr;
  }

}
void KProfiler::Init(jvmtiEnv *jvmtiEnv) {
  if (instance != nullptr) {
    //already Init
    return;
  }
  instance = new KProfiler(jvmtiEnv);
  instance->AddMethodEventListener(nullptr);

}

KProfiler *KProfiler::Get() {
  return instance;
}

