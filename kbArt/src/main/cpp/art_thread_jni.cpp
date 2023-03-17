#include <jni.h>
#include "art_thread.h"

//
// Created by Knight-ZXW on 2023/6/1.
//


static jfieldID nativePeerField = NULL;

extern "C"
JNIEXPORT jint JNICALL
Java_com_knightboost_artvm_ArtThread_getTid(JNIEnv *env,
                                            jclass clazz,
                                            jobject thread) {
  if (nativePeerField == nullptr) {
    jclass threadClass = env->GetObjectClass(thread);
    nativePeerField = env->GetFieldID(threadClass, "nativePeer", "J");
  }
  jlong nativePeerValue = env->GetLongField(thread, nativePeerField);
  if (nativePeerValue == 0){ //native thread has not yet been created/started, or has been destroyed.
    return -1;
  }
  auto *artThread = reinterpret_cast<kbArt::Thread *>(nativePeerValue);
  uint32_t tid = artThread->GetTid();
  //double check, 保证上面计算tid时，线程还未被销毁，避免计算出一个异常的tid
  nativePeerValue = env->GetLongField(thread, nativePeerField);
  if (nativePeerValue == 0){ //native thread has not yet been created/started, or has been destroyed.
    return -1;
  }
  return tid;
}