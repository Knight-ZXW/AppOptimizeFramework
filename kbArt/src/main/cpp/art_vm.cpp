//
// Created by Knight-ZXW on 2023/6/1.
//
#include <jni.h>
#include "art.h"
#include "tls.h"
using namespace kbArt;
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  JNIEnv *env = nullptr;
  if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
    return -1;
  }
  ArtHelper::init(env);
  return JNI_VERSION_1_6;
}

