//
// Created by knight-zxw on 2023/1/2.
// Email: nimdanoob@163.com
//

#include "sample_trace.h"
#include "vector"
#include "include/art.h"
#include "art_method.h"
#include "art_thread.h"

using namespace kbArt;
using namespace sliver;
static void GetSample(Thread* thread){

}

static void LogMethodTraceEvent(Thread* thread,ArtMethod* method,TraceAction action,
                                uint32_t thread_clock_diff,uint32_t wall_clock_diff){



}

void CompareAndUpdateStackTrace(
    void *thread,
    std::vector<ArtMethod *> *old_stack_trace,
    std::vector<ArtMethod *> *stack_trace) {

  uint32_t thread_clock_diff = 0;
  uint32_t wall_clock_diff = 0;

  if (old_stack_trace == nullptr) {
    // If there's not previous stack trace sample for this thread, log an entry event for all
    // methods in the trace

    for (auto rit = stack_trace->rbegin(); rit != stack_trace->rend(); ++rit) {
      // LogMethodTraceEvent(thread, *rit, instrumentation::Instrumentation::kMethodEntered,
      //                          thread_clock_diff, wall_clock_diff);
    }
  } else {
    // If there's a previous stack trace for this thread, diff the traces and emit entry and exit
    // events accordingly
    auto old_rit = old_stack_trace->rbegin();
    auto rit = stack_trace->rbegin();

    // Iterate bottom-up over both traces until there's a difference between them.
    while (old_rit !=old_stack_trace->rend() && rit != stack_trace->rend() && *old_rit == *rit){
      old_rit++;
      rit++;
    }

    //Iterate top-down over the old trace until the point where they differ. emitting exit events
    for(auto old_it =  old_stack_trace->begin(); old_it != old_rit.base(); ++old_it){
      //LogMethodTraceEvent(thread,*old_it,kMethodExited,thread_clock_diff,wall_clock_diff);
    }

    //Iterate bottom-up over the new trace from the point where they differ, emitting entry events.
    for(;rit!=stack_trace->rend();++rit){
      //LogMethodTraceEvent(thread, *rit, instrumentation::Instrumentation::kMethodEntered,
      //                          thread_clock_diff, wall_clock_diff);
    }

  }

}
void sliver::Trace::dump(int trace_file_fd) {

}
