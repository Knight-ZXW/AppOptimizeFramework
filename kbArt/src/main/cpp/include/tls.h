#pragma once


#if defined(__aarch64__)
# define __get_tls() ({ void** __val; __asm__("mrs %0, tpidr_el0" : "=r"(__val)); __val; })
#elif defined(__arm__)
# define __get_tls() ({ void** __val; __asm__("mrc p15, 0, %0, c13, c0, 3" : "=r"(__val)); __val; })
#elif defined(__i386__)
# define __get_tls() ({ void** __val; __asm__("movl %%gs:0, %0" : "=r"(__val)); __val; })
#elif defined(__riscv)
# define __get_tls() ({ void** __val; __asm__("mv %0, tp" : "=r"(__val)); __val; })
#elif defined(__x86_64__)
# define __get_tls() ({ void** __val; __asm__("mov %%fs:0, %0" : "=r"(__val)); __val; })
#else
#error unsupported architecture
#endif

#if defined(__arm__) || defined(__aarch64__)
#define TLS_SLOT_ART_THREAD_SELF  7
#elif defined(__i386__) || defined(__x86_64__)
#define TLS_SLOT_ART_THREAD_SELF  7
#elif defined(__riscv)
#define TLS_SLOT_ART_THREAD_SELF (-1)
#endif
