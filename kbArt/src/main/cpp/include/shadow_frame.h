//
// Created by knight-zxw on 2022/12/30.
//

#ifndef KB_SHADOW_FRAME_H_
#define KB_SHADOW_FRAME_H_

#include <jni.h>
#include <memory>

namespace kbArt {

class LockCountData {
  void *monitors_;
};

// ref: https://cs.android.com/android/platform/superproject/+/master:art/runtime/interpreter/shadow_frame.h;drc=add3d02a9be211efeafa6cff8b27bdf7e44e4081;l=50
// ShadowFrame has 2 possible layouts:
//  - interpreter - separate VRegs and reference arrays. References are in the reference array.
//  - JNI - just VRegs, but where every VReg holds a reference.
class ShadowFrame {
 private:
  // Used to keep track of extra state the shadowframe has.
  enum class FrameFlags : uint32_t {
    // We have been requested to notify when this frame gets popped.
    kNotifyFramePop = 1 << 0,
    // We have been asked to pop this frame off the stack as soon as possible.
    kForcePopFrame = 1 << 1,
    // We have been asked to re-execute the last instruction.
    kForceRetryInst = 1 << 2,
    // Mark that we expect the next frame to retry the last instruction (used by instrumentation and
    // debuggers to keep track of required events)
    kSkipMethodExitEvents = 1 << 3,
    // Used to suppress exception events caused by other instrumentation events.
    kSkipNextExceptionEvent = 1 << 4,
  };
 public:
  // Link to previous shadow frame or null;
  ShadowFrame *link_;
  // ArtMethod* method_;
  void *method;
  //JValue* result_register_;
  void *result_register_;

  const uint16_t *dex_pc_ptr_;
  // Dex instruction base of the code item.
  const uint16_t *dex_instructions_;
  LockCountData lockCountData;
  const uint32_t dex_pc_;
  int16_t cached_hotness_countdown_;
  int16_t hotness_countdown_;
  // This is a set of ShadowFrame::FrameFlags which denote special states this frame is in.
  // NB alignment requires that this field takes 4 bytes no matter its size. Only 3 bits are
  // currently used.
  uint32_t frame_flags_;

  // This is a two-part array:
  //  - [0..number_of_vregs) holds the raw virtual registers, and each element here is always 4
  //    bytes.
  //  - [number_of_vregs..number_of_vregs*2) holds only reference registers. Each element here is
  //    ptr-sized.
  // In other words when a primitive is stored in vX, the second (reference) part of the array will
  // be null. When a reference is stored in vX, the second (reference) part of the array will be a
  // copy of vX.
  uint32_t vregs_[0];

 public:
  ~ShadowFrame() {}

};
}

#endif //KB_SHADOW_FRAME_H_
