#include <jni.h>
#include "include/art.h"
#include "sliver/fetch_stack_visitor.h"
#include "vector"
#include <chrono>
#include "logger.h"
#include "logger.h"
#include "art_thread.h"
using namespace kbArt;


extern "C"
JNIEXPORT jlongArray JNICALL
Java_com_knightboost_sliver_Sliver_nativeGetMethodStackTrace(JNIEnv *env,
                                                             jclass clazz,
                                                             jobject threadPeer,
                                                             jlong native_peer) {

  auto* thread = reinterpret_cast<Thread *>(native_peer);

  bool timeOut;

  Thread *current_thread = Thread::Current();

  bool isSameThread = false;
  if (current_thread == thread){
    isSameThread = true;
    LOGE("sliver","在当前线程获取调用栈");
  }
  if (!isSameThread){
    ArtHelper::SuspendThreadByThreadId(thread->GetThreadId(),
                                       SuspendReason::kForUserCode,
                                       &timeOut);
  }

  
  std::vector<std::uintptr_t> stack_methods;
  auto f = [](ArtMethod *method, void *visitorData) -> bool {
    auto *methods = reinterpret_cast<std::vector<std::uintptr_t> *>(visitorData);
    methods->push_back(reinterpret_cast<std::uintptr_t>(&*method));
    return true;
  };

  FetchStackVisitor visitor(thread,
                            &stack_methods,
                            f);
  ArtHelper::StackVisitorWalkStack(&visitor, false);

  if (!isSameThread){
    ArtHelper::Resume(thread, SuspendReason::kForUserCode);
  }

  std::vector<double> results(4);
  jdoubleArray output = env->NewDoubleArray(4);
  env->SetDoubleArrayRegion(output, 0, 4, &results[0]);

  jlongArray methodArray = env->NewLongArray((jsize) stack_methods.size());

  jlong *destElements = env->GetLongArrayElements(methodArray, nullptr);
  for (int i = 0; i < stack_methods.size(); i++) {
    destElements[i] =(jlong) stack_methods[i];
  }
  env->ReleaseLongArrayElements(methodArray, destElements, 0);
  return methodArray;
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_knightboost_sliver_Sliver_prettyMethods(JNIEnv *env, jclass clazz, jlongArray methods) {
  jlong *methods_ptr = env->GetLongArrayElements(methods, nullptr);
  jsize size = env->GetArrayLength(methods);
  //TODO cache StringClass
  jobjectArray ret = env->NewObjectArray(size, env->FindClass("java/lang/String"),
                                         nullptr);

  for (int i = 0; i < size; i++) {
    const std::string
        &pretty_method = ArtHelper::PrettyMethod(reinterpret_cast<void *>(methods_ptr[i]), true);
    pretty_method.c_str();
    jstring frame = env->NewStringUTF(pretty_method.c_str());
    env->SetObjectArrayElement(ret, i, frame);
    env->DeleteLocalRef(frame);
  }
  env->ReleaseLongArrayElements(methods,methods_ptr,0);
  return ret;

}