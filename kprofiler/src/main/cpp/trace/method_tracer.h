//
// Created by Knight-ZXW on 2023/6/12.
//

#ifndef KPROFILER_TRACE_METHOD_TRACER_H_
#define KPROFILER_TRACE_METHOD_TRACER_H_
#include "listeners.h"
class MethodTracer : public MethodEventListener {

 public:

  void MethodEntry(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread, jmethodID method) override;

  void MethodExit(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread,
                  jmethodID method, jboolean was_popped_by_exception, jvalue return_value) override;
};

#endif //KPROFILER_TRACE_METHOD_TRACER_H_
