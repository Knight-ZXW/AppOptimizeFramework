//
// Created by knight-zxw on 2023/1/2.
// Email: nimdanoob@163.com
//

#pragma once
#include "include/stackvisitor.h"
using namespace kbArt;
namespace sliver {

enum TraceAction {
  kTraceMethodEnter = 0x00, // method entry
  kTraceMethodExit = 0x01,  // method exit
  kTraceUnroll = 0x02,      // method exited by exception unrolling
};

class Trace {

 public:
  void dump(int trace_file_fd);


};

}

template<typename T>
__attribute__ ((always_inline)) static void WalkStack(const T &fn,
                                                      void *thread) {

  class LambdaStackVisitor : public StackVisitor {
   public:
    LambdaStackVisitor(const T &fn, void *thread) : StackVisitor(
        thread,
        StackWalkKind::kIncludeInlinedFrames
    ) {
      this->fn_ =fn;
    }
    
    bool VisitFrame() override {
      return true;
    }
   private:
    T fn_;

  };

}