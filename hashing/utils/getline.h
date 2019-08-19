#ifndef HASH_GETLINE_H
#define HASH_GETLINE_H

#include <stdio.h>

#ifdef __cplusplus
extern "C" {
#endif

// if typedef doesn't exist (msvc, blah)
typedef intptr_t ssize_t;

ssize_t getline(char **lineptr, size_t *n, FILE *stream);

#ifdef __cplusplus
}
#endif

#endif