//
// Created by Knight-ZXW on 2023/6/12.
//

#include "method_tracer.h"
#include "logger.h"
#include "art.h"
#include <android/trace.h>
void MethodTracer::MethodEntry(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread, jmethodID method) {
  LOGE("MethodTrace","method entry ");
}
void MethodTracer::MethodExit(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread, jmethodID method, jboolean was_popped_by_exception, jvalue return_value) {
  LOGE("MethodTrace","method exit ");
}
