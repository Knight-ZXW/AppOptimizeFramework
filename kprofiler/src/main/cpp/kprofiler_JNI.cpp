//
// Created by Knight-ZXW on 2023/6/9.
//

//
// Created by Knight-ZXW on 2023/4/14.
//
#include <jni.h>
#include <cstdio>
#include "logger.h"
#include "jvmti.h"
#include "jvmagent/agent_env.h"
#include "jvmagent/class_load_tracer.h"
#include "jvmagent/jvmti_helper.h"
#include "method_tracer.h"

#include <jni.h>
#include <cstring>
#include "logger.h"
#include <android/log.h>
#include "kprofiler.h"

//JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
//  JNIEnv *jenv;
//  if (vm->GetEnv((void **) &jenv, JNI_VERSION_1_6) != JNI_OK) {
//    return JNI_ERR;
//  }
//  jvmtiAgent::g_vm = vm;
//  return JNI_VERSION_1_6;
//}

static jvmtiEnv *g_jvmti;

jint AgentStart(JavaVM *vm, char *options, void *reserved) {
  jvmtiEnv *jvmti = nullptr;
  if (vm->GetEnv(reinterpret_cast<void **>(&jvmti), JVMTI_VERSION) != JNI_OK || jvmti == nullptr) {
    LOGE("jvmAgent", "obtain JVMTI env failed");
    return JNI_ERR;
  }

  g_jvmti = jvmti;
  KProfiler::Init(jvmti);
  LOGE("kprofiler", "Start OK");
  return JNI_OK;
}

extern "C" JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *jvm, char *options, void *reserved) {
  LOGD("kprofiler", "kprofielr onLoad");
  return AgentStart(jvm, options, reserved);
}

/**
 *  called when agent so  is attached
 */
extern "C" JNIEXPORT jint  JNICALL Agent_OnAttach(JavaVM *jvm, char *options, void *reserved) {
  LOGD("kprofiler", "Kprofiler onAttach");
  return AgentStart(jvm, options, reserved);
}

jboolean startMonitorClassLoadOfThread(JNIEnv *env,
                                       jclass clazz, jstring file_path,
                                       jobject target_thread) {
  LOGE("jvmAgent", "start Monitor class Load");

  const char *c_path = env->GetStringUTFChars(file_path, NULL);
  LOGE("jvmAgent", "1");

//    callbacks.ClassLoad = ClassLoadCallback;
  auto *tracer = new ClassLoadTracer(c_path);
  if (target_thread != nullptr) {
    jobject target_thread_ref = env->NewGlobalRef(target_thread);
    tracer->targetThread = target_thread_ref;
  }

  ClassLoadTracer::setCurTracer(tracer);
  int32_t code = tracer->attachJvmti(g_jvmti);

  if (code == 0) {
    return JNI_TRUE;
  } else {
    return JNI_FALSE;
  }
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_knightboost_kprofiler_KProfiler_nStartMonitorClassLoad(JNIEnv *env,
                                                                jclass clazz,
                                                                jstring file_path) {
  return startMonitorClassLoadOfThread(env, clazz, file_path, nullptr);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_knightboost_kprofiler_KProfiler_nStartMonitorClassLoadOfThread(JNIEnv *env,
                                                                        jclass clazz,
                                                                        jstring file_path,
                                                                        jthread target_thread) {
  return startMonitorClassLoadOfThread(env, clazz, file_path, target_thread);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_knightboost_kprofiler_KProfiler_nStopMonitorClassLoad(JNIEnv *env, jclass clazz) {
  g_jvmti->SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_CLASS_LOAD, nullptr);
  g_jvmti->SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_CLASS_PREPARE, nullptr);
  ClassLoadTracer::getTracer()->stop();
  return JNI_TRUE;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_knightboost_kprofiler_KProfiler_nRecordClassLoadMsg(JNIEnv *env, jclass clazz, jstring msg) {
  const char *c_msg = env->GetStringUTFChars(msg, nullptr);
  ClassLoadTracer::getTracer()->recordMsg(c_msg);
// 释放字符数组所占的内存
  env->ReleaseStringUTFChars(msg, c_msg);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_knightboost_kprofiler_KProfiler_testMethodTrace(JNIEnv *env, jclass clazz) {
  std::unique_ptr<MethodTracer> tracer(new MethodTracer);
  KProfiler::Get()->AddMethodEventListener(tracer.get());
};
