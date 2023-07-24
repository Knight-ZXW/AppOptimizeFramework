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
#include "jvmagent/jvmti_helper.h"

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





