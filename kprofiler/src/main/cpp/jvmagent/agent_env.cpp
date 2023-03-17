//
// Created by Knight-ZXW on 2023/4/17.
//

#include "agent_env.h"

namespace jvmtiAgent {
    JavaVM *g_vm = nullptr;
    jvmtiEnv *jvmti_env = nullptr;
    jvmtiEventCallbacks current_callbacks = {};

    static bool gRuntimeIsJVM = false;

    bool IsJVM() {
        return gRuntimeIsJVM;
    }

    void SetJVM(bool b) {
        gRuntimeIsJVM = b;
    }

}  // namespace art