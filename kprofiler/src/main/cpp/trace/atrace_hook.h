//
// Created by Knight-ZXW on 2023/6/18.
//

#pragma once

#include <string>
#include <vector>
#include <unordered_set>
#include <utils/macros.h>

namespace kprofiler {
    namespace atrace {
        class ATraceHook {
        public:
            static ATraceHook &Get();

            bool IsHooked() const { return hook_ok_; }

            bool HookLoadedLibs();

            bool UnhookLoadedLibs();

        private:
            ATraceHook();

            ~ATraceHook();

            static std::unordered_set<std::string> s_seen_libs_;

            bool hook_init_{false};
            bool hook_ok_{false};

            DISALLOW_COPY_AND_ASSIGN(ATraceHook);
        };
    }
}

