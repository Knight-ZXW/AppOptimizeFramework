//
// Created by Knight-ZXW on 2023/4/17.
//

#include "jvmti_helper.h"
#include "jvmti.h"
#include <android/log.h>
#include "logger.h"
#include "agent_env.h"

namespace jvmtiAgent {
    void CheckJvmtiError(jvmtiEnv *env, jvmtiError error) {
        if (error != JVMTI_ERROR_NONE) {
            char *error_name;
            jvmtiError name_error = env->GetErrorName(error, &error_name);
            if (name_error != JVMTI_ERROR_NONE) {
                LOGF("jvmtiAgent", "Unable to get error name for %u", error);

            }
            LOGF("jvmtiAgent", "Unexpected error: %u", error);
        }
    }

    // These are a set of capabilities we will enable in all situations. These are chosen since they
    // will not affect the runtime in any significant way if they are enabled.
    static const jvmtiCapabilities standard_caps = {
            .can_tag_objects                                 = 1,
            .can_generate_field_modification_events          = 1,
            .can_generate_field_access_events                = 1,
            .can_get_bytecodes                               = 1,
            .can_get_synthetic_attribute                     = 1,
            .can_get_owned_monitor_info                      = 0,
            .can_get_current_contended_monitor               = 1,
            .can_get_monitor_info                            = 1,
            .can_pop_frame                                   = 0,
            .can_redefine_classes                            = 1,
            .can_signal_thread                               = 1,
            .can_get_source_file_name                        = 1,
            .can_get_line_numbers                            = 1,
            .can_get_source_debug_extension                  = 1,
            .can_access_local_variables                      = 0,
            .can_maintain_original_method_order              = 1,
            .can_generate_single_step_events                 = 1,
            .can_generate_exception_events                   = 0,
            .can_generate_frame_pop_events                   = 0,
            .can_generate_breakpoint_events                  = 1,
            .can_suspend                                     = 1,
            .can_redefine_any_class                          = 0,
            .can_get_current_thread_cpu_time                 = 0,
            .can_get_thread_cpu_time                         = 0,
            .can_generate_method_entry_events                = 1,
            .can_generate_method_exit_events                 = 1,
            .can_generate_all_class_hook_events              = 0,
            .can_generate_compiled_method_load_events        = 0,
            .can_generate_monitor_events                     = 0,
            .can_generate_vm_object_alloc_events             = 1,
            .can_generate_native_method_bind_events          = 1,
            .can_generate_garbage_collection_events          = 1,
            .can_generate_object_free_events                 = 1,
            .can_force_early_return                          = 0,
            .can_get_owned_monitor_stack_depth_info          = 0,
            .can_get_constant_pool                           = 0,
            .can_set_native_method_prefix                    = 0,
            .can_retransform_classes                         = 1,
            .can_retransform_any_class                       = 0,
            .can_generate_resource_exhaustion_heap_events    = 0,
            .can_generate_resource_exhaustion_threads_events = 0,
    };

    jvmtiCapabilities GetStandardCapabilities() {
        return standard_caps;
    }
    void SetStandardCapabilities(jvmtiEnv* env) {
        jvmtiCapabilities caps = GetStandardCapabilities();
        CheckJvmtiError(env, env->AddCapabilities(&caps));
    }

    void SetAllCapabilities(jvmtiEnv* env) {
        jvmtiCapabilities caps;
        CheckJvmtiError(env, env->GetPotentialCapabilities(&caps));
        CheckJvmtiError(env, env->AddCapabilities(&caps));
    }
}
