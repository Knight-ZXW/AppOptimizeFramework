//
// Created by Knight-ZXW on 2023/7/1.
//


#pragma once

#include <android/log.h>

#ifndef NDBUG

#ifndef LOG_TAG
#define LOG_TAG  "KProfiler"
#endif

#define ALOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define ALOGF(...) __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, __VA_ARGS__)

#define ASSERT(cond, ...) if (!(cond)) {__android_log_assert(#cond, LOG_TAG, __VA_ARGS__);}

#else

#define ALOGV(...)
#define ALOGD(...)
#define ALOGI(...)
#define ALOGW(...)
#define ALOGE(...)
#define ALOGF(...)
#define ASSERT(cond, ...)

#endif

/* XXX Macros to work around syntax errors in places where format string
* arg is not passed to ALOG_ASSERT, LOG_ALWAYS_FATAL or LOG_ALWAYS_FATAL_IF
* (happens only in debug builds).
*/

/* Returns 2nd arg.  Used to substitute default value if caller's vararg list
 * is empty.
 */
#define __android_second(dummy, second, ...)     second

/* If passed multiple args, returns ',' followed by all but 1st arg, otherwise
 * returns nothing.
 */
#define __android_rest(first, ...)               , ## __VA_ARGS__

#define android_printAssert(cond, tag, fmt...) \
    __android_log_assert(cond, tag, \
        __android_second(0, ## fmt, NULL) __android_rest(fmt))

/*
* Log a fatal error.  If the given condition fails, this stops program
* execution like a normal assertion, but also generating the given message.
* It is NOT stripped from release builds.  Note that the condition test
* is -inverted- from the normal assert() semantics.
*/
#ifndef LOG_ALWAYS_FATAL_IF
#define LOG_ALWAYS_FATAL_IF(cond, ...) \
    ( (__predict_false(cond)) \
    ? ((void)android_printAssert(#cond, LOG_TAG, ## __VA_ARGS__)) \
    : (void)0 )
#endif

#define LIKELY(x) (__builtin_expect((x), 1))
#define UNLIKELY(x) (__builtin_expect((x), 0))