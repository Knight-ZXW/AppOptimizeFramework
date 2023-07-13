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

  static void* getArtSoHandle();

  static void *getThreadList();

  static void StackVisitorWalkStack(StackVisitor *visitor, bool include_transitions);

  //Suspend a thread using a peer.
  static void *suspendThreadByPeer(jobject peer, SuspendReason reason, bool *timed_out);

    // Suspend a thread using its thread id, typically used by lock/monitor inflation. Returns the
    // thread on success else null. The thread id is used to identify the thread to avoid races with
    // the thread terminating. Note that as thread ids are recycled this may not suspend the expected
    // thread, that may be terminating. If the suspension times out then *timeout is set to true.
  static void *SuspendThreadByThreadId(uint32_t threadId,
                                       SuspendReason suspendReason,
                                       bool *timed_out);

  static JniIdManager *getJniIdManager();

  static bool Resume(void *thread, SuspendReason suspendReason);

  static bool SetJdwpAllowed(bool allowed);

  static bool IsJdwpAllow();

  static bool SetJavaDebuggable(bool debuggable);

  static bool DisableClassVerify();

  static bool  EnableClassVerify();

  static bool  DelayJit();

  static bool  ResumeJit();

  static std::string PrettyMethod(void *art_method, bool with_signature);

 private:
  static int load_symbols();

 public:
  static void *runtime;
  static void *partialRuntime;
  static JniIdManager* jniIdManager;
 private:
  static char* artPath;


};
}

#endif //KB_ART_H_
