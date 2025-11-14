#include <jni.h>
#include <vector>
#include <android/log.h>
#include <cstring>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "native-lib", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "native-lib", __VA_ARGS__)

// Simple edge-like processor (no OpenCV) - grayscale + sobel-like gradient + threshold

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_example_edgeviewer_NativeBridge_processImageNative(JNIEnv *env, jclass,
                                                           jbyteArray inBytes, jint width,
                                                           jint height) {
    if (inBytes == nullptr) return nullptr;
    jsize len = env->GetArrayLength(inBytes);
    if (len != width * height * 4) {
        LOGE("Input length mismatch: %d vs expected %d", len, width * height * 4);
        return nullptr;
    }

    jbyte* inBuf = env->GetByteArrayElements(inBytes, NULL);
    if (!inBuf) return nullptr;

    const unsigned char* src = reinterpret_cast<unsigned char*>(inBuf);
    std::vector<unsigned char> gray(width * height);
    for (int i = 0; i < width * height; ++i) {
        int r = src[i*4 + 0] & 0xFF;
        int g = src[i*4 + 1] & 0xFF;
        int b = src[i*4 + 2] & 0xFF;
        gray[i] = static_cast<unsigned char>((0.299*r + 0.587*g + 0.114*b));
    }

    std::vector<unsigned char> edges(width * height, 0);

    for (int y = 1; y < height-1; ++y) {
        for (int x = 1; x < width-1; ++x) {
            int idx = y*width + x;
            int gx = 0;
            int gy = 0;
            gx = -gray[(y-1)*width + (x-1)] - 2*gray[y*width + (x-1)] - gray[(y+1)*width + (x-1)]
                 + gray[(y-1)*width + (x+1)] + 2*gray[y*width + (x+1)] + gray[(y+1)*width + (x+1)];
            gy = -gray[(y-1)*width + (x-1)] - 2*gray[(y-1)*width + x] - gray[(y-1)*width + (x+1)]
                 + gray[(y+1)*width + (x-1)] + 2*gray[(y+1)*width + x] + gray[(y+1)*width + (x+1)];
            int mag = abs(gx) + abs(gy);
            edges[idx] = std::min(255, mag);  // keeps gradients
        }
    }

    std::vector<unsigned char> out(width * height * 4);
    for (int i = 0; i < width * height; ++i) {
        unsigned char e = edges[i];
        out[i*4 + 0] = e;
        out[i*4 + 1] = e;
        out[i*4 + 2] = e;
        out[i*4 + 3] = 255;
    }

    jbyteArray outArr = env->NewByteArray(width * height * 4);
    env->SetByteArrayRegion(outArr, 0, width*height*4, reinterpret_cast<const jbyte*>(out.data()));
    env->ReleaseByteArrayElements(inBytes, inBuf, JNI_ABORT);
    return outArr;
}
