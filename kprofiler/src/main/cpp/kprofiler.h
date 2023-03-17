//
// Created by Knight-ZXW on 2023/6/9.
//

#pragma once
#include "jvmti.h"
#include "listeners.h"
#include "kprofiler.h"
#include "vector"

class KProfiler {
 public:
  static void Init(jvmtiEnv *jvmtiEnv);
  static KProfiler *Get();
  void AddMethodEventListener(MethodEventListener *listener);
  void RemoveMethodEventListener(MethodEventListener *listener);
  virtual ~KProfiler() {}
 private:
  KProfiler(jvmtiEnv *jvmti_env);
 private:
  jvmtiEnv *jvmti_env_;
  jvmtiEventCallbacks *jvmti_event_callbacks_;
  std::vector<MethodEventListener*> methodEventsListeners;

 protected:
  static KProfiler *instance;

};
