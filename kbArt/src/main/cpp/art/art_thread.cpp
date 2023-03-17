//
// Created by Knight-ZXW on 2023/6/1.
//
#include "art_thread.h"

uint32_t kbArt::Thread::GetThreadId() {
  static uint32_t offset = 0;
  if (offset == 0) {
    int api_level = android_get_device_api_level();
    if (api_level >= __ANDROID_API_S__) { // >=Android 12
      offset = offsetof(tls_32bit_sized_values, thin_lock_thread_id);
    } else if (api_level <= __ANDROID_API_Q__) { // <=Android 10
      offset = offsetof(tls_32bit_sized_values_android10, thin_lock_thread_id);
    }
  }
  return *(uint32_t *) ((char *) this + offset);
}

uint32_t kbArt::Thread::GetTid() {
  static uint32_t offset = 0;
  if (offset == 0) {
    int api_level = android_get_device_api_level();
    if (api_level >= __ANDROID_API_S__) { // >=Android 12
      offset = offsetof(tls_32bit_sized_values, tid);
    } else if (api_level <= __ANDROID_API_Q__) { // <=Android 10
      offset = offsetof(tls_32bit_sized_values_android10, tid);
    }
  }
  return *(uint32_t *) ((char *) this + offset);
}

kbArt::Thread *kbArt::Thread::Current() {
  void *thread = __get_tls()[TLS_SLOT_ART_THREAD_SELF];
  return reinterpret_cast<Thread *>(thread);
}
