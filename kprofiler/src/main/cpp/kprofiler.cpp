#include "kprofiler.h"
#include "logger.h"
#include "string"
#include "jvmagent/jvmti_helper.h"
#include "jvmti.h"
#include "art.h"
using namespace kbArt;
//https://github.com/tiann/epic/blob/master/library/src/main/cpp/epic.cpp
static void profilerJvmtiEventMethodEntry
    (jvmtiEnv *jvmti_env,
     JNIEnv *jni_env,
     jthread thread,
     jmethodID method) {
  LOGE("KProfiler","profilerJvmtiEventMethodEntry");
  static jmethodID  getNameMethodId;

  char* name;
  char* sig;
  char* gen;

  bool isIndexId = (reinterpret_cast<uintptr_t>(method) % 2) != 0;
  void* artMethod = ArtHelper::getJniIdManager()->DecodeMethodId(method);
  LOGE("KProfiler","artMethod is %p",artMethod);
  const std::string
      &pretty_method = kbArt::ArtHelper::PrettyMethod(artMethod, true);
  LOGE("KProfiler","pretty is %s",pretty_method.c_str());
//  jclass declaringClass;
//  jvmti_env->GetMethodDeclaringClass(method,&declaringClass);
//  jni_env->CallObjectMethod(declaringClass,)
  jvmti_env->GetMethodName(method,&name,&sig,&gen);
  LOGE("KProfiler","method name is %s %s %s",name,sig,gen);
}

static void profilerJvmtiEventMethodExit
    (jvmtiEnv *jvmti_env,
     JNIEnv *jni_env,
     jthread thread,
     jmethodID method,
     jboolean was_popped_by_exception,
     jvalue return_value) {
  LOGE("KProfiler","profilerJvmtiEventMethodExit");
}

KProfiler* KProfiler::instance = nullptr;

KProfiler::KProfiler(jvmtiEnv *jvmti_env) : jvmti_env_(jvmti_env) {
  auto *callbacks = new jvmtiEventCallbacks;
  memset(callbacks, 0, sizeof(*callbacks));
  jvmti_event_callbacks_ = callbacks;
  int error = jvmti_env->SetEventCallbacks(callbacks,sizeof (*callbacks));
  if (error!=JVMTI_ERROR_NONE){
    LOGE("KProfiler","error on Kprofiler setEventCallbacks");
  }
  jvmtiAgent::SetStandardCapabilities(jvmti_env);

}

void KProfiler::AddMethodEventListener(MethodEventListener *listener) {
  //TODO lock
  methodEventsListeners.push_back(listener);
  LOGE("KProfiler","开始监控函数");
  if (methodEventsListeners.size() == 1) {

    jvmti_event_callbacks_->MethodEntry = profilerJvmtiEventMethodEntry;
    jvmti_event_callbacks_->MethodExit = profilerJvmtiEventMethodExit;
    int error = jvmti_env_->SetEventCallbacks(jvmti_event_callbacks_,sizeof (*jvmti_event_callbacks_));
    LOGE("KProfiler","添加Listener %d",error);
    jvmtiError error1 = jvmti_env_->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY, nullptr);
    LOGE("KProfiler","设置notification mode %d",error1);
    jvmtiError error2 = jvmti_env_->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_EXIT, nullptr);
    LOGE("KProfiler","设置notification mode2 %d",error2);

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
  if (instance!= nullptr){
    //already Init
    return;
  }
  instance = new KProfiler(jvmtiEnv);
}

KProfiler *KProfiler::Get() {
  return instance;
}

