//
// Created by Administrator on 2024/3/25.
//
#include <xdl.h>
#include "art_xdl.h"
#include "common.h"

void *get_art_handle() {
  static void* handle = nullptr;
  if (handle == nullptr){
    char *artPath = getLibArtPath();
     handle = xdl_open(artPath,
                            XDL_TRY_FORCE_LOAD);
  }
  return handle;
}
