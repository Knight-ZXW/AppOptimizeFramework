//
// Created by Knight-ZXW on 2023/7/2.
//

#include "native_trace_callbacks.h"
#define LOG_TAG "KProfiler.NativeTraceCallback"
#include <utils/debug.h>
#include <cinttypes>

namespace kprofiler {
namespace atrace {

void NativeTraceCallbacks::OnTraceStart(
    int64_t trace_id, int32_t flags, std::string trace_file) {
  ALOGE("OnTraceStart: trace_id=%" PRId64 ", flags=%d, trace_file=%s",
        trace_id, flags, (char*)trace_file.c_str());
}

void NativeTraceCallbacks::OnTraceEnd(int64_t trace_id) {
  ALOGE("OnTraceEnd: trace_id=%" PRId64, trace_id);
}

void NativeTraceCallbacks::OnTraceAbort(int64_t trace_id, AbortReason reason) {
  ALOGE("OnTraceAbort: trace_id=%" PRId64 ", abort reason=%d", trace_id, reason);
}

}  // namespace kprofiler
}  // namespace bytedance