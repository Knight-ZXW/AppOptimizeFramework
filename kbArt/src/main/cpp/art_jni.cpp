#include <jni.h>
#include "include/art.h"

//
// Created by Knight-ZXW on 2023/6/8.
//

extern "C"

using namespace kbArt;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_knightboost_artvm_KbArt_nSetJdwpAllowed(JNIEnv *env, jclass clazz, jboolean allowed) {
  return ArtHelper::SetJdwpAllowed(allowed);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_knightboost_artvm_KbArt_nSetJavaDebuggable(JNIEnv *env, jclass clazz, jboolean debuggable) {
  return ArtHelper::SetJavaDebuggable(debuggable);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_knightboost_artvm_KbArt_nIsJdwpAllow(JNIEnv *env, jclass clazz) {
  return ArtHelper::IsJdwpAllow();
}