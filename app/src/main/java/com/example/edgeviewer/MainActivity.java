package com.example.edgeviewer;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.view.Gravity;
import android.view.ViewGroup;

import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {

    private static final String TAG = "EdgeViewer";
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);

        imageView = new ImageView(this);
        FrameLayout.LayoutParams ivlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        ivlp.gravity = Gravity.CENTER;
        imageView.setLayoutParams(ivlp);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        root.addView(imageView);
        setContentView(root);

        Log.i(TAG, "Loading test.png from assets…");

        Bitmap bmp = loadTestBitmapFromAssets(getAssets(), "test.png");
        if (bmp == null) {
            Log.e(TAG, "Failed to load test.png");
            return;
        }

        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int[] pixels = new int[w * h];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);

        // Convert ARGB → RGBA
        byte[] rgba = new byte[w * h * 4];
        for (int i = 0; i < w * h; i++) {
            int c = pixels[i];
            rgba[i * 4 + 0] = (byte) ((c >> 16) & 0xFF); // R
            rgba[i * 4 + 1] = (byte) ((c >> 8) & 0xFF);  // G
            rgba[i * 4 + 2] = (byte) (c & 0xFF);         // B
            rgba[i * 4 + 3] = (byte) ((c >> 24) & 0xFF); // A
        }

        // Process in C++
        byte[] out = NativeBridge.processImageNative(rgba, w, h);
        if (out == null) {
            Log.e(TAG, "Native processing failed.");
            imageView.setImageBitmap(bmp);
            return;
        }

        // Convert RGBA back to ARGB
        int[] outPixels = new int[w * h];
        for (int i = 0; i < w * h; i++) {
            int r = out[i * 4] & 0xFF;
            int g = out[i * 4 + 1] & 0xFF;
            int b = out[i * 4 + 2] & 0xFF;
            int a = out[i * 4 + 3] & 0xFF;
            outPixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        Bitmap outBmp = Bitmap.createBitmap(outPixels, w, h, Bitmap.Config.ARGB_8888);
        imageView.setImageBitmap(outBmp);

        // NEW: Save output to /web/processed.png
        saveBitmapToWebFolder(outBmp);

        Log.i(TAG, "Finished native processing.");
    }

    // Load image from assets
    private Bitmap loadTestBitmapFromAssets(AssetManager am, String name) {
        try (InputStream is = am.open(name)) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeStream(is, null, opts);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load asset: " + name, e);
            return null;
        }
    }

    // Save processed result into /web/processed.png
    private void saveBitmapToWebFolder(Bitmap bmp) {
        try {
            File webDir = new File(getExternalFilesDir(null), "../web");
            if (!webDir.exists()) webDir.mkdirs();

            File outFile = new File(webDir, "processed.png");
            FileOutputStream fos = new FileOutputStream(outFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Log.i(TAG, "Saved processed image to: " + outFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Failed to save processed image", e);
        }
    }
}
