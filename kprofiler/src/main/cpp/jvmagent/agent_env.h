//
// Created by Knight-ZXW on 2023/4/17.
//

#pragma once
#include "jvmti.h"
namespace jvmtiAgent{
    extern JavaVM* g_vm;
    extern jvmtiEnv* jvmti_env;

// This is a jvmtiEventCallbacks struct that is used by all common ti-agent code whenever it calls
// SetEventCallbacks. This can be used by single tests to add additional event callbacks without
// being unable to use the rest of the ti-agent support code.
    extern jvmtiEventCallbacks current_callbacks;

}