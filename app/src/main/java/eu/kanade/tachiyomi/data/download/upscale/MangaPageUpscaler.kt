package eu.kanade.tachiyomi.data.download.upscale

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.tumuyan.realsr.RealCugan
import java.io.File
import java.io.FileOutputStream

object MangaPageUpscaler {
    private val cugan = RealCugan()
    private var initialized = false

    @Synchronized
    fun init(context: Context): Boolean {
        if (initialized) return true
        if (!RealCugan.isLibraryLoaded) return false

        try {
            val modelDir = File(context.filesDir, "models")
            if (!modelDir.exists()) {
                modelDir.mkdirs()
            }
            // Initialize with GPU ID 0, 2x scale, noise level 0, and models directory path
            val success = cugan.init(0, 2, 0, modelDir.absolutePath)
            if (success) {
                initialized = true
                return true
            }
        } catch (e: Exception) {
            Log.e("MangaPageUpscaler", "Failed to initialize RealCUGAN: ${e.message}")
        }
        return false
    }

    fun upscalePage(inputFile: File, outputFile: File): Boolean {
        if (!initialized) return false
        return try {
            val bitmapIn = BitmapFactory.decodeFile(inputFile.absolutePath) ?: return false
            
            // Create an empty destination bitmap at exactly double the size (2x upscaling)
            val bitmapOut = Bitmap.createBitmap(bitmapIn.width * 2, bitmapIn.height * 2, bitmapIn.config)
            
            // Execute the native C++ process matching RealCugan.kt's signature
            val success = cugan.process(bitmapIn, bitmapOut)
            if (success) {
                FileOutputStream(outputFile).use { out ->
                    bitmapOut.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                bitmapIn.recycle()
                bitmapOut.recycle()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("MangaPageUpscaler", "Error upscaling page: ${inputFile.name}", e)
            false
        }
    }
}
