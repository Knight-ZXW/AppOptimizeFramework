//
// Created by Knight-ZXW on 2023/6/12.
//
#ifndef KPROFILER_LISTENERS
#define KPROFILER_LISTENERS

#include "jvmti.h"

class MethodEventListener {
 public:
  virtual ~MethodEventListener() {};
  virtual void MethodEntry(jvmtiEnv *jvmti_env,
                           JNIEnv *jni_env,
                           jthread thread,
                           jmethodID method) = 0;
  virtual void MethodExit (jvmtiEnv *jvmti_env,
                           JNIEnv *jni_env,
                           jthread thread,
                           jmethodID method,
                           jboolean was_popped_by_exception,
                           jvalue return_value) =0;
};

#endif //KPROFILER_LISTENERS
