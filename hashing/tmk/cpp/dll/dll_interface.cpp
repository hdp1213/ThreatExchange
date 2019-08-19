#include "dll_interface.h"
#include <tmk/cpp/hashing/filehasher.h>

// ----------------------------------------------------------------
std::string basename(const std::string& path, const std::string& delimiter) {
  size_t n = path.length();
  size_t i = path.rfind(delimiter, n);
  if (i == std::string::npos) {
    return path;
  } else {
    return path.substr(i + 1, n - i);
  }
}

// ----------------------------------------------------------------
std::string stripExtension(
    const std::string& path,
    const std::string& delimiter) {
  size_t n = path.length();
  size_t i = path.rfind(delimiter, n);
  if (i == std::string::npos) {
    return path;
  } else {
    return path.substr(0, i);
  }
}

// ----------------------------------------------------------------
bool TmkPdqfHashVideoFile(const std::string& inputVideoFileName,
    const std::string& ffmpegPath)
{
    facebook::tmk::io::TMKFramewiseAlgorithm tmkFramewiseAlgorithm = facebook::tmk::io::TMKFramewiseAlgorithm::PDQ_FLOAT;
    bool verbose = false;
    const char* argv0 = "";
    int resampleFramesPerSecond = 15;
    std::string outputFeatureVectorsFileName = "";

    facebook::tmk::algo::TMKFeatureVectors tmkFeatureVectors;

    bool rc = facebook::tmk::hashing::hashVideoFile(
      inputVideoFileName,
      tmkFramewiseAlgorithm,
      ffmpegPath,
      resampleFramesPerSecond,
      tmkFeatureVectors,
      verbose,
      argv0);

    if (!rc) {
        return false;
    }

    std::string b = basename(inputVideoFileName, "/");
    b = stripExtension(b, ".");
    outputFeatureVectorsFileName = "./" + b + ".tmk";

    FILE* outputFp = facebook::tmk::io::openFileOrDie(
      outputFeatureVectorsFileName.c_str(), "wb", argv0);

    if (!tmkFeatureVectors.writeToOutputStream(outputFp, argv0)) {
        return false;
    }
    fclose(outputFp);

    return true;
}