package com.example.service.ai

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

sealed class VisionResult {
    data class Success(val summary: String, val isFromLiveAi: Boolean) : VisionResult()
    data class Error(val message: String, val fallbackSummary: String? = null) : VisionResult()
}

class AiVisionService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeDefectPhoto(
        bitmap: Bitmap,
        provider: String = "GEMINI",
        apiKey: String,
        language: String = "fr",
        fallbackHint: String? = null
    ): VisionResult = withContext(Dispatchers.IO) {
        val langInstruction = when (language) {
            "ar" -> "Respond strictly in Arabic (العربية)."
            "en" -> "Respond strictly in English."
            else -> "Respond strictly in French (Français)."
        }

        val systemPrompt = "You are a professional construction site inspector. " +
                "Analyze this photo, identify the defect, and generate a very short, professional defect summary " +
                "(e.g., 'Manque plinthe', 'Peinture écaillée', 'Fissure sur enduit'). " +
                "Keep it under 6 words. Output ONLY the defect summary. No quotes, no intro, no punctuation. $langInstruction"

        // If no API key is provided, return intelligent inspector fallback
        if (apiKey.isBlank() || apiKey.contains("MY_GEMINI_API_KEY")) {
            val fallback = getContextualFallback(fallbackHint, language)
            return@withContext VisionResult.Success(
                summary = fallback,
                isFromLiveAi = false
            )
        }

        try {
            val base64Image = bitmapToBase64(bitmap)
            val resultText = if (provider.equals("OPENAI", ignoreCase = true)) {
                callOpenAiVision(apiKey, base64Image, systemPrompt)
            } else {
                callGeminiVision(apiKey, base64Image, systemPrompt)
            }

            val cleaned = cleanDefectText(resultText, fallbackHint, language)
            VisionResult.Success(summary = cleaned, isFromLiveAi = true)
        } catch (e: Exception) {
            val fallback = getContextualFallback(fallbackHint, language)
            VisionResult.Error(
                message = e.localizedMessage ?: "Erreur de communication avec l'API Vision",
                fallbackSummary = fallback
            )
        }
    }

    private fun callGeminiVision(apiKey: String, base64Image: String, prompt: String): String {
        // Model: gemini-3.5-flash
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val partText = JSONObject().put("text", prompt)
        val inlineData = JSONObject()
            .put("mimeType", "image/jpeg")
            .put("data", base64Image)
        val partImage = JSONObject().put("inlineData", inlineData)

        val parts = JSONArray().put(partText).put(partImage)
        val content = JSONObject().put("parts", parts)
        val contents = JSONArray().put(content)

        val generationConfig = JSONObject()
            .put("temperature", 0.2)
            .put("maxOutputTokens", 40)

        val jsonPayload = JSONObject()
            .put("contents", contents)
            .put("generationConfig", generationConfig)

        val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errJson = JSONObject(responseBody)
                errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
            } catch (_: Exception) {
                "HTTP ${response.code}: $responseBody"
            }
            throw RuntimeException(errorMsg)
        }

        val jsonResponse = JSONObject(responseBody)
        val candidates = jsonResponse.optJSONArray("candidates")
        val firstCandidate = candidates?.optJSONObject(0)
        val contentObj = firstCandidate?.optJSONObject("content")
        val partsArr = contentObj?.optJSONArray("parts")
        val text = partsArr?.optJSONObject(0)?.optString("text")

        return text?.trim() ?: throw RuntimeException("Réponse vide de l'API")
    }

    private fun callOpenAiVision(apiKey: String, base64Image: String, prompt: String): String {
        val url = "https://api.openai.com/v1/chat/completions"

        val systemMsg = JSONObject()
            .put("role", "system")
            .put("content", prompt)

        val userContent = JSONArray()
        userContent.put(JSONObject().put("type", "text").put("text", "Identify the construction defect in this photo in under 6 words."))
        val imageObj = JSONObject().put("url", "data:image/jpeg;base64,$base64Image")
        userContent.put(JSONObject().put("type", "image_url").put("image_url", imageObj))

        val userMsg = JSONObject()
            .put("role", "user")
            .put("content", userContent)

        val messages = JSONArray().put(systemMsg).put(userMsg)

        val payload = JSONObject()
            .put("model", "gpt-4o-mini")
            .put("messages", messages)
            .put("max_tokens", 30)

        val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errJson = JSONObject(responseBody)
                errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
            } catch (_: Exception) {
                "HTTP ${response.code}: $responseBody"
            }
            throw RuntimeException(errorMsg)
        }

        val jsonResponse = JSONObject(responseBody)
        val choices = jsonResponse.optJSONArray("choices")
        val message = choices?.optJSONObject(0)?.optJSONObject("message")
        return message?.optString("content")?.trim() ?: throw RuntimeException("Réponse vide d'OpenAI")
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        // Downscale bitmap if too large for bandwidth optimization
        val maxDimension = 1024
        val scaled = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val (w, h) = if (ratio > 1f) {
                maxDimension to (maxDimension / ratio).toInt()
            } else {
                (maxDimension * ratio).toInt() to maxDimension
            }
            Bitmap.createScaledBitmap(bitmap, w, h, true)
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun cleanDefectText(raw: String, fallbackHint: String?, language: String): String {
        var text = raw.replace("\n", " ").replace("\"", "").replace("*", "").trim()
        val words = text.split(Regex("\\s+"))
        if (words.size > 8) {
            text = words.take(6).joinToString(" ")
        }
        if (text.isBlank()) {
            return getContextualFallback(fallbackHint, language)
        }
        return text
    }

    fun getContextualFallback(hint: String?, language: String): String {
        val h = hint?.lowercase() ?: ""
        return when (language) {
            "ar" -> when {
                h.contains("plinthe") -> "نقص في الوزرة السفلية"
                h.contains("fissure") -> "تشقق في الطلاء والجبس"
                h.contains("peinture") -> "تقشر في دهان الجدار"
                else -> "خلل في التشطيبات المكتملة"
            }
            "en" -> when {
                h.contains("plinthe") -> "Missing baseboard"
                h.contains("fissure") -> "Plaster wall crack"
                h.contains("peinture") -> "Flaking paint on wall"
                else -> "Finishing defect observed"
            }
            else -> when { // Default French
                h.contains("plinthe") -> "Manque plinthe"
                h.contains("fissure") -> "Fissure sur enduit"
                h.contains("peinture") -> "Peinture écaillée"
                else -> "Défaut de finition constaté"
            }
        }
    }
}
