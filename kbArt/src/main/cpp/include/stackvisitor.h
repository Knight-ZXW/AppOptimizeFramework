//
// Created by knight-zxw on 2022/12/29.
//

#ifndef KB_STACKVISITOR_H_
#define KB_STACKVISITOR_H_

#include "shadow_frame.h"
namespace kbArt {

const size_t STRUCT_COMPAT = sizeof(size_t) * 200;

class StackVisitor {
 public:
  enum class StackWalkKind {
    kIncludeInlinedFrames,
    kSkipInlinedFrames
  };

 protected:
  StackVisitor(void *thread, StackWalkKind walk_kind)
      : walk_kind_(walk_kind),
        thread_(thread) {}

 public:

  virtual ~StackVisitor() {};

  // Return 'true' if we should continue to visit more frames, 'false' to stop.
  virtual bool VisitFrame() = 0;

  void *thread_ = nullptr;
  const StackWalkKind walk_kind_ = StackWalkKind::kIncludeInlinedFrames;
  ShadowFrame *cur_shadow_frame_ = nullptr;
  void **cur_quick_frame_ = nullptr;

  //保证有足够的空间存放其他变量; detail see:
  //https://cs.android.com/android/platform/superproject/+/master:art/runtime/stack.h
  char param[STRUCT_COMPAT] = {};
 public:
  inline void *GetMethod() {
    if (cur_shadow_frame_ != nullptr) {
      return cur_shadow_frame_->method;
    } else if (cur_quick_frame_ != nullptr) {
      return *cur_quick_frame_;
    }
    return nullptr;
  }
};

}

#endif //KB_STACKVISITOR_H_
