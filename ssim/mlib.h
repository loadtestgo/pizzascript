#ifndef MLB_DOT_H_INCLUDED
#define MLB_DOT_H_INCLUDED

#include <stdio.h>

#ifdef _MSC_VER
typedef unsigned char      ml_uint8;
typedef unsigned short     mlib_uint16;
typedef   signed short     mlib_int16;
typedef unsigned int       mlib_uint32;
typedef   signed int       mlib_int32;
typedef unsigned long long mlib_uint64;
typedef long long          mlib_int64;
#else
#include <stdint.h>
typedef uint8_t  ml_uint8;
typedef uint16_t ml_uint16;
typedef int16_t  ml_int16;
typedef uint32_t ml_uint32;
typedef int32_t  ml_int32;
typedef uint64_t ml_uint64;
typedef int64_t  ml_int64;
#endif

FILE *ml_fopen(char const *filename, char const *mode);

#ifdef MLB_IMPLEMENTATION
FILE *ml_fopen(char const *filename, char const *mode)
{
    FILE *f;
#if defined(_MSC_VER) && _MSC_VER >= 1400
    if (0 != fopen_s(&f, filename, mode))
      f=0;
#else
    f = fopen(filename, mode);
#endif
    return f;
}
#endif

#endif
