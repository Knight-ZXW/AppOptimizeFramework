//
// Created by Knight-ZXW on 2023/4/14.
//

#include "class_load_tracer.h"
#include <jni.h>
#include "jvmti.h"
#include "utils/timers.h"
#include "logger.h"
#include <cinttypes>
#include "jvmti_helper.h"
#include "agent_env.h"
#include <unistd.h>
#include <sstream>

static const int kBufferSize = 1024 * 80;

static ClassLoadTracer *curTracer = nullptr;

void ClassLoadTracer::setCurTracer(ClassLoadTracer *tracer) {
  curTracer = tracer;
}

ClassLoadTracer *ClassLoadTracer::getTracer() {
  return curTracer;
}

void ClassLoadTracer::recordMsg(const char *msg) {
  std::stringstream ss;
  ss << gettid() << ":" << uptimeMillis() << ":" << msg << "\n";
  std::string log = ss.str();

  LOGE("classLoader", " %s", msg);

  while (mutex_.test_and_set()) {
  }

  buffer += log;
  if (buffer.length() >= kBufferSize) {
    fwrite(buffer.c_str(), buffer.length(), 1, writeFile);
    fflush(writeFile);
    buffer.clear();
  }
  mutex_.clear();

}

void ClassLoadTracer::stop() {
  if (this->writeFile == nullptr) {
    return;
  }
  if (buffer.length() > 0) {
    fwrite(buffer.c_str(), buffer.length(), 1, writeFile);
  }
  fflush(this->writeFile);
  //todo clear memory
  buffer.clear();
  this->writeFile = nullptr;
}

ClassLoadTracer::ClassLoadTracer(const char *filePath) :
    buffer(),
    mutex_(),
    targetThread(nullptr) {
  this->writeFile = fopen(filePath, "w+");
  if (writeFile == nullptr) {
    LOGF("class_load", "fopen %s failed", filePath);
  }
}

ClassLoadTracer::~ClassLoadTracer() {
  if (targetThread != nullptr) {
    JNIEnv *env = getEnv();
    env->DeleteGlobalRef(targetThread);
    targetThread = nullptr;
  }
}

void ClassLoadTracer::ClassPrepareCallback(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jclass clazz) {
  jvmtiError err;

// 获取类的名称
  char *class_sig;
  err = jvmti->GetClassSignature(clazz, &class_sig, NULL);
  if (err != JVMTI_ERROR_NONE || class_sig == NULL) {
    return;
  }
  if (curTracer->targetThread != nullptr && !env->IsSameObject(thread, curTracer->targetThread)) {
    return;
  }

  curTracer->recordMsg(class_sig);
  jvmti->Deallocate((unsigned char *) class_sig);
}

JNIEnv *ClassLoadTracer::getEnv() {
  JNIEnv *env = nullptr;
  jvmtiAgent::g_vm->GetEnv((void **) &env, JNI_VERSION_1_6);
  return env;
}

int32_t ClassLoadTracer::attachJvmti(jvmtiEnv *jvmtiEnv) {
  jvmtiEventCallbacks callbacks;
  memset(&callbacks, 0, sizeof(callbacks));
  callbacks.ClassPrepare = ClassLoadTracer::ClassPrepareCallback;
  LOGE("jvmAgent", "2");

  int error = jvmtiEnv->SetEventCallbacks(&callbacks, sizeof(callbacks));
  if (error != JVMTI_ERROR_NONE) {
    LOGE("jvmAgent", "Error on Agent_OnAttach: %d", error);
    return JNI_ERR;
  }
  LOGE("jvmAgent", "3");

  jvmtiEnv->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_LOAD, nullptr);
  jvmtiEnv->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_PREPARE, nullptr);
  LOGE("jvmAgent", "4");
  return 0;
}


