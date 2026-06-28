package eu.kanade.tachiyomi.data.download.upscale

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.tumuyan.realsr.RealCugan
import java.io.File
import java.io.FileOutputStream
import logcat.LogPriority
import tainacompleter.util.lang.logcat

object MangaPageUpscaler {

    private var isInitialized = false
    private val realCugan = RealCugan()

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            // Initialize RealCUGAN native NCNN environment
            // Model 2 means 2x upscaling optimized for anime/manga structures
            realCugan.init(2, 2, context.assets)
            isInitialized = true
            logcat(LogPriority.INFO) { "RealCUGAN successfully initialized for manga upscaling" }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to initialize RealCUGAN native library" }
        }
    }

    fun upscalePage(inputFile: File, outputFile: File): Boolean {
        if (!isInitialized || !inputFile.exists()) return false

        return try {
            val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath) ?: return false
            
            // Execute the native 2x super-resolution processing
            val upscaledBitmap = realCugan.process(bitmap) ?: return false

            FileOutputStream(outputFile).use { out ->
                upscaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            
            bitmap.recycle()
            upscaledBitmap.recycle()
            true
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Error upscaling page: ${inputFile.name}" }
            false
        }
    }
}

