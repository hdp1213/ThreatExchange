#ifndef DLL_INTERFACE_H
#define DLL_INTERFACE_H

#include <string>

#ifdef __cplusplus
extern "C" {
#endif

#ifdef TMK_EXPORTS
  #ifdef __GNUC__
    #define TMK_API __attribute__ ((dllexport))
  #else
    #define TMK_API __declspec(dllexport) // Note: actually gcc seems to also supports this syntax.
  #endif
#else
  #ifdef __GNUC__
    #define TMK_API __attribute__ ((dllimport))
  #else
    #define TMK_API __declspec(dllimport) // Note: actually gcc seems to also supports this syntax.
  #endif
#endif

#define TMK_CALL __cdecl

bool TMK_API TmkPdqfHashVideoFile(const std::string& inputVideoFileName,
    const std::string& ffmpegPath);

#ifdef __cplusplus
}
#endif

#endif