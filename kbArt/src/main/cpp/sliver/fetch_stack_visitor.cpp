//
// Created by knight-zxw on 2022/12/31.
//
#include "include/stackvisitor.h"
#include "include/art.h"
#include "art_method.h"
#include "logger.h"
#include "fetch_stack_visitor.h"
using namespace kbArt;

bool FetchStackVisitor::VisitFrame() {
  void *method = GetMethod();
  auto *artMethod = static_cast<ArtMethod *>(method);
  if (artMethod == nullptr || artMethod->IsRuntimeMethod()) {
    return true;
  }
//    if (frame_count == 0) {
//      void *lock;
//      uint32_t tid;
//      //Top frame, check for blocked state
//      ThreadState state = ArtHelper::FetchState(thread_, &lock, &tid);
//      if (state == ThreadState::kBlocked || state == ThreadState::kWaitingForLockInflation) {
//        //TODO 记录锁等待信息. 调用 PrettyTypeOf 打印锁信息
//        LOGW("fetchStackVisitor", "blocked or waiting on object  %p",lock);
//      }
//    }

  if (StackVisitorCallback != nullptr) {
    return StackVisitorCallback(artMethod, visitorData);
  }
  frame_count++;
  return true;
}

FetchStackVisitor::FetchStackVisitor(void *thread,
                                     void *visitorData,
                                     bool (*callback)(ArtMethod *,
                                                      void *)) : StackVisitor(thread,
                                                                              StackWalkKind::kIncludeInlinedFrames),
                                                                 frame_count(0) {
  this->thread_ = thread;
  this->StackVisitorCallback = callback;
  this->visitorData = visitorData;
}
