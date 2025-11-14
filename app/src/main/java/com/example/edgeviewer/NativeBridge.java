package com.example.edgeviewer;

public class NativeBridge {

    static {
        try {
            System.loadLibrary("native-lib");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    // Must match native signature in C++
    public static native byte[] processImageNative(byte[] rgba, int width, int height);
}
