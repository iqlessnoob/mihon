package eu.kanade.tachiyomi.data.download

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.tumuyan.realsr.RealCugan // Class name exposed by the RealCugan-NCNN dependency
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RealCuganUpscaler {
    private var isInitialized = false
    private val realsr = RealCugan()

    // Initialize RealCUGAN with model assets 
    private suspend fun initialize(context: Context) = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext
        try {
            // Copy model files from assets to app private storage so NCNN can read them
            val modelDir = File(context.filesDir, "models")
            if (!modelDir.exists()) modelDir.mkdirs()

            // RealCUGAN model assets from the dependency's bundle
            context.assets.list("models")?.forEach { fileName ->
                context.assets.open("models/$fileName").use { input ->
                    FileOutputStream(File(modelDir, fileName)).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            // Load RealCUGAN 2x model (parameters: gpuid, model_dir, model_name)
            // Model names: "realcugan-se", "realcugan-pro"
            realsr.init(0, 2, 0, modelDir.absolutePath)


            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Upscales a manga page image file by 2x using RealCUGAN.
     */
    suspend fun upscaleImageFile(context: Context, file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            initialize(context)
            if (!isInitialized) return@withContext false

            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@withContext false
            
            // Allocate output bitmap for 2x upscaling
            val upscaledBitmap = Bitmap.createBitmap(
                bitmap.width * 2,
                bitmap.height * 2,
                Bitmap.Config.ARGB_8888
            )

            // Run the super-resolution model 2x scale
            val success = realsr.process(bitmap, upscaledBitmap)

            if (success) {
                FileOutputStream(file).use { out ->
                    upscaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                bitmap.recycle()
                upscaledBitmap.recycle()
                true
            } else {
                bitmap.recycle()
                upscaledBitmap.recycle()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
