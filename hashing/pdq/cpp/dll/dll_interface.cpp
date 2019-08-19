#include "dll_interface.h"

#include <pdq/cpp/io/pdqio.h>
#include <string.h>
#include <stdexcept>


// ----------------------------------------------------------------
bool PDQ_CALL PdqfHashFile(const char* inputFileName, char* fileHash, size_t fileHashLen) {
  facebook::pdq::hashing::Hash256 pdqhash;
  int quality;
  int imageHeightTimesWidthUnused = 0;
  float readSecondsUnused = 0.0;
  float hashSecondsUnused = 0.0;
  const char* argv0 = "";

  try {
    facebook::pdq::hashing::pdqHash256FromFile(
      inputFileName,
      pdqhash,
      quality,
      imageHeightTimesWidthUnused,
      readSecondsUnused,
      hashSecondsUnused
    );
  } catch (std::runtime_error& e) {
    fprintf(stderr, "%s: could not decode \"%s\".\n", argv0, inputFileName);
    return false;
  }

  strncpy(fileHash, pdqhash.format().c_str(), fileHashLen);
  return true;
}
