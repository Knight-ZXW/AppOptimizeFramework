//
// Created by knight-zxw on 2022/12/31.
//

#ifndef KB_art_method_H_
#define KB_art_method_H_

#include "modifiers.h"
namespace kbArt {
class ArtMethod final {
 public:
  static constexpr uint32_t kRuntimeMethodDexMethodIndex = 0xFFFFFFFF;
  // Field order required by test "ValidateFieldOrderOfJavaCppUnionClasses".
  // The class we are a part of.
  //GcRoot<mirror::Class> declaring_class_;
  uint32_t declaring_class_;

  // Access flags; low 16 bits are defined by spec.
  // Getting and setting this flag needs to be atomic when concurrency is
  // possible, e.g. after this method's class is linked. Such as when setting
  // verifier flags and single-implementation flag.
  //std::atomic<std::uint32_t> access_flags_;
  std::atomic<uint32_t> access_flags_;

  /* Dex file fields. The defining dex file is available via declaring_class_->dex_cache_ */

  // Index into method_ids of the dex file associated with this method.
  uint32_t dex_method_index_;

  /* End of dex file fields. */

  // Entry within a dispatch table for this method. For static/direct methods the index is into
  // the declaringClass.directMethods, for virtual methods the vtable and for interface methods the
  // ifTable.
  uint16_t method_index_;

  union {
    // Non-abstract methods: The hotness we measure for this method. Not atomic,
    // as we allow missing increments: if the method is hot, we will see it eventually.
    uint16_t hotness_count_;
    // Abstract methods: IMT index.
    uint16_t imt_index_;
  };

  uint32_t GetAccessFlags() const {
    return access_flags_.load(std::memory_order_relaxed);
  }

  inline bool IsRuntimeMethod() const {
    return dex_method_index_ == kRuntimeMethodDexMethodIndex;
  }

  // Returns true if the method is static, private, or a constructor.
  bool IsDirect() const {
    return IsDirect(GetAccessFlags());
  }

  static bool IsDirect(uint32_t access_flags) {
    constexpr uint32_t direct = kAccStatic | kAccPrivate | kAccConstructor;
    return (access_flags & direct) != 0;
  }


};

}

#endif //KB_art_method_H_
