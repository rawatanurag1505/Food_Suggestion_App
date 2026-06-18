package com.example.api

import com.example.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList()
)

@Serializable
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

private val jsonInstance = Json { ignoreUnknownKeys = true; coerceInputValues = true }

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(jsonInstance.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

@Serializable
data class MealSuggestion(
    val name: String,
    val description: String,
    val reason: String,
    val recipe: String,
    val time: String,
    val cost: String,
    val nutrition: String,
    val swap: String
)

suspend fun suggestMeals(prompt: String): List<MealSuggestion> = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        throw Exception("API key is missing. Please add it in the Secrets panel.")
    }
    
    val request = GenerateContentRequest(
        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
        generationConfig = GenerationConfig(
            temperature = 0.7f,
            responseMimeType = "application/json"
        ),
        systemInstruction = Content(parts = listOf(Part(text = "You are a personal food assistant. Suggest 3 meals. You MUST return a JSON array of objects, with each object having exactly these string keys: name, description, reason, recipe, time, cost, nutrition, swap.")))
    )

    try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        val textResponse = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
        
        if (textResponse != null) {
            try {
                return@withContext jsonInstance.decodeFromString<List<MealSuggestion>>(textResponse)
            } catch (e: Exception) {
                return@withContext emptyList()
            }
        } else {
             return@withContext emptyList()
        }
    } catch (e: retrofit2.HttpException) {
        val errorBody = e.response()?.errorBody()?.string() ?: "Unknown HTTP error"
        throw Exception("HTTP ${e.code()}: $errorBody")
    } catch (e: Exception) {
        throw e
    }
}
