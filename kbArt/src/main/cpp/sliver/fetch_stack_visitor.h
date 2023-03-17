//
// Created by knight-zxw on 2023/1/2.
// Email: nimdanoob@163.com
//
#pragma once
#include "include/stackvisitor.h"
#include "art_method.h"
using namespace kbArt;
class FetchStackVisitor : public StackVisitor {
 public:
  FetchStackVisitor(void *thread,
                    void *visitorData,
                    bool (*callback)(ArtMethod *,
                                     void *));

 protected:
  size_t frame_count;

 private:
  bool (*StackVisitorCallback)(ArtMethod *art_method, void *visitorData) = nullptr;
  void *visitorData = nullptr;
  bool VisitFrame();
};