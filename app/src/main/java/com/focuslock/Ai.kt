package com.focuslock

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object Ai {
    suspend fun ask(prompt: String, history: List<String>): String = withContext(Dispatchers.IO) {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) return@withContext "Add GEMINI_API_KEY to local.properties and rebuild."
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"
            val c = URL(url).openConnection() as HttpURLConnection
            c.requestMethod = "POST"
            c.setRequestProperty("content-type", "application/json")
            c.setRequestProperty("x-goog-api-key", BuildConfig.GEMINI_API_KEY)
            c.doOutput = true
            val system = "You are a concise, encouraging study coach inside a focus app. Suggest realistic session lengths, breaks and study tips. Recent sessions: $history"
            val body = JSONObject()
                .put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
                .put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt.take(1000))))))
            c.outputStream.use { it.write(body.toString().toByteArray()) }
            val stream = if (c.responseCode in 200..299) c.inputStream else c.errorStream
            val j = JSONObject(stream.bufferedReader().readText())
            j.optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")
                ?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                ?: j.optJSONObject("error")?.optString("message") ?: "AI error"
        } catch (e: Exception) { "Network error: ${e.message}" }
    }
}