//
// Created by knight-zxw on 2023/1/2.
// Email: nimdanoob@163.com
//
#pragma once

#define PACKED(x) __attribute__ ((__aligned__(x), __packed__))
#define ALWAYS_INLINE  __attribute__ ((always_inline))


// Changing this definition will cause you a lot of pain.  A majority of
// vendor code defines LIKELY and UNLIKELY this way, and includes
// this header through an indirect path.
#define LIKELY( exp )       (__builtin_expect( (exp) != 0, true  ))
#define UNLIKELY( exp )     (__builtin_expect( (exp) != 0, false ))