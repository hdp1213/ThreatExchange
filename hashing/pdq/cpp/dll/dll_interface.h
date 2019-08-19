#ifndef DLL_INTERFACE_H
#define DLL_INTERFACE_H

#include <stdlib.h>

#ifdef __cplusplus
extern "C" {
#endif

#ifdef PDQ_EXPORTS
    #define PDQ_API __declspec(dllexport) // Note: actually gcc seems to also supports this syntax.
#else
    #define PDQ_API __declspec(dllimport) // Note: actually gcc seems to also supports this syntax.
#endif

#define PDQ_CALL __cdecl

bool PDQ_API PdqfHashFile(const char* inputFileName, char* fileHash, size_t fileHashLen);

#ifdef __cplusplus
}
#endif

#endif