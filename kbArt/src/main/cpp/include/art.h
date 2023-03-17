//
// Created by Administrator on 2022/12/28.
// Email: nimdanoob@163.com
//

#ifndef KB_ART_H_
#define KB_ART_H_
#include <stdint.h>
#include <jni.h>
#include <string>
#include "stackvisitor.h"
#include "shadow_frame.h"
#include "common.h"
#include "art_method.h"
#include "art_def.h"
namespace kbArt {

class JniIdManager {
 public:
   JniIdManager(void *runtime,void *instanceRef);
   void * DecodeMethodId(jmethodID methodId);
 private:
   void *_instanceRef;


};

class ArtHelper {

 public:
  static int init(JNIEnv *env);

  static void* findArtDsym(const char *symbol, size_t *symbol_size);

  static void *getThreadList();

  static void StackVisitorWalkStack(StackVisitor *visitor, bool include_transitions);

  //Suspend a thread using a peer.
  static void *suspendThreadByPeer(jobject peer, SuspendReason reason, bool *timed_out);

  // Suspend a thread using its thread id, typically used by lock/monitor inflation. Returns the
  // thread on success else null.
  static void *SuspendThreadByThreadId(uint32_t threadId,
                                       SuspendReason suspendReason,
                                       bool *timed_out);

  static JniIdManager *getJniIdManager();

  static bool Resume(void *thread, SuspendReason suspendReason);

  static bool SetJdwpAllowed(bool allowed);

  static bool IsJdwpAllow();

  static bool SetJavaDebuggable(bool debuggable);

  static std::string PrettyMethod(void *art_method, bool with_signature);

  static ThreadState FetchState(void *thread, void *monitor_object, uint32_t *lock_owner_tid);

  static uint64_t GetCpuMicroTime(void *thread);

 private:
  static int load_symbols();

 public:
  static void *artHandle;
  static void *runtime;
  static void *partialRuntime;
  static JniIdManager* jniIdManager;

};
}

#endif //KB_ART_H_
