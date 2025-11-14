# EdgeViewer - Emulator-ready (Minimal) 

This repository is an **emulator-ready** minimal implementation of the Edge Detection Viewer assessment.
It demonstrates:
- Android app (Java) with emulator fallback (loads a static image from assets)
- JNI (C++) native processing (simple edge detector, no OpenCV dependency)
- OpenGL ES 2.0 renderer (textured quad) used on devices; emulator uses ImageView fallback
- Web viewer (TypeScript) showing a sample processed frame

## How to run (recommended)

1. Open the project in Android Studio.
2. Start an Android emulator (x86_64 or arm64). 
3. Build and run the `app` module.

The emulator will show a processed static image (assets/test.png) processed by native C++ and displayed.

