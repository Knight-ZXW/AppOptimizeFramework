//
// Created by Knight-ZXW on 2023/4/14.
//

#include "timers.h"


#include <time.h>

#if defined(__linux__)
nsecs_t systemTime(int clock)
{
    static const clockid_t clocks[] = {
            CLOCK_REALTIME,
            CLOCK_MONOTONIC,
            CLOCK_PROCESS_CPUTIME_ID,
            CLOCK_THREAD_CPUTIME_ID,
            CLOCK_BOOTTIME
    };
    struct timespec t;
    t.tv_sec = t.tv_nsec = 0;
    clock_gettime(clocks[clock], &t);
    return nsecs_t(t.tv_sec)*1000000000LL + t.tv_nsec;
}
#else
nsecs_t systemTime(int /*clock*/)
{
  // Clock support varies widely across hosts. Mac OS doesn't support
  // CLOCK_BOOTTIME, and Windows is windows.
  struct timeval t;
  t.tv_sec = t.tv_usec = 0;
  gettimeofday(&t, nullptr);
  return nsecs_t(t.tv_sec)*1000000000LL + nsecs_t(t.tv_usec)*1000LL;
}
#endif

/*
 * native public static long uptimeMillis();
 */
int64_t uptimeMillis()
{
    int64_t when = systemTime(SYSTEM_TIME_MONOTONIC);
    return (int64_t) nanoseconds_to_milliseconds(when);
}

/*
 * native public static long elapsedRealtimeMillis();
 */
int64_t elapsedRealtimeMillis()
{
    int64_t when = systemTime(SYSTEM_TIME_BOOTTIME);
    return (int64_t) nanoseconds_to_milliseconds(when);
}

/*
 * native public static long elapsedRealtimeMillis();
 */
int64_t elapsedRealtimeMicros()
{
    int64_t when = systemTime(SYSTEM_TIME_BOOTTIME);
    return (int64_t) nanoseconds_to_microseconds(when);
}


int64_t elapsedRealtimeNanos()
{
    return systemTime(SYSTEM_TIME_MONOTONIC);
}

