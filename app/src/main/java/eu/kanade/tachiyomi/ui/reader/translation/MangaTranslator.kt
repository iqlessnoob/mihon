package eu.kanade.tachiyomi.ui.reader.translation

import android.graphics.Bitmap
import android.util.Base64
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object MangaTranslator {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun translateMangaPage(
        bitmap: Bitmap,
        apiKey: String,
        targetLanguage: String
    ): Result<List<SpeechBubble>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Gemini API key is not set. Please set it in Settings."))
        }

        val base64Image = try {
            bitmap.toBase64()
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Failed to process image: ${e.message}"))
        }

        val prompt = """
            Analyze this manga page. Find all text segments and speech bubbles.
            Translate the text from its original language to $targetLanguage.
            
            Return a JSON array of objects, where each object represents a speech bubble with the following fields:
            1. "original": The exact text from the original speech bubble.
            2. "translated": The translated text in $targetLanguage.
            3. "top": Float representing the top edge coordinate of the bubble as a percentage of total image height (0.0 to 100.0).
            4. "left": Float representing the left edge coordinate of the bubble as a percentage of total image width (0.0 to 100.0).
            5. "width": Float representing the width of the bubble as a percentage of total image width (0.0 to 100.0).
            6. "height": Float representing the height of the bubble as a percentage of total image height (0.0 to 100.0).
            
            Return ONLY the valid JSON array. Do not wrap it in markdown codeblocks or prefix it. Ensure all coordinate values are accurate percentages of the image size.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Empty response received from Gemini API."))

            val listType = Types.newParameterizedType(List::class.java, SpeechBubble::class.java)
            val adapter = moshi.adapter<List<SpeechBubble>>(listType)
            
            val cleanedJsonText = jsonText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val bubbles = adapter.fromJson(cleanedJsonText)
                ?: return@withContext Result.failure(Exception("Failed to parse speech bubble coordinates JSON."))

            Result.success(bubbles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
