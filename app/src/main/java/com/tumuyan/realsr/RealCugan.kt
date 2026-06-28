package com.tumuyan.realsr

import android.graphics.Bitmap
import android.util.Log

class RealCugan {
    companion object {
        var isLibraryLoaded = false
        init {
            try {
                System.loadLibrary("realcugan")
                isLibraryLoaded = true
            } catch (e: Throwable) {
                Log.e("RealCugan", "Failed to load realcugan native library: ${e.message}")
            }
        }
    }

    external fun init(gpuid: Int, scale: Int, noise: Int, model: String): Boolean
    external fun process(bitmapIn: Bitmap, bitmapOut: Bitmap): Boolean
}

