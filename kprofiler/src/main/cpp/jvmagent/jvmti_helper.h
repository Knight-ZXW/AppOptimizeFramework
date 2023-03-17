//
// Created by Knight-ZXW on 2023/4/17.
//

#pragma once

#include <memory>
#include <ostream>

#include "jni.h"
#include "jvmti.h"
namespace jvmtiAgent{

// Get a standard set of capabilities for use in tests.
    jvmtiCapabilities GetStandardCapabilities();

// Add all the standard capabilities to the given env.
    void SetStandardCapabilities(jvmtiEnv* env);

// Add all capabilities to the given env.
// TODO Remove this in the future.
    void SetAllCapabilities(jvmtiEnv* env);

// Check whether the given error is NONE. If not, print out the corresponding error message
// and abort.
    void CheckJvmtiError(jvmtiEnv* env, jvmtiError error);

// Convert the given error to a RuntimeException with a message derived from the error. Returns
// true on error, false if error is JVMTI_ERROR_NONE.
    bool JvmtiErrorToException(JNIEnv* env, jvmtiEnv* jvmtienv, jvmtiError error);


}
