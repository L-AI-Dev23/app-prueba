package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<ContentPart>
)

@JsonClass(generateAdapter = true)
data class ContentPart(
    val parts: List<TextPart>
)

@JsonClass(generateAdapter = true)
data class TextPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<CandidatePart>?
)

@JsonClass(generateAdapter = true)
data class CandidatePart(
    val content: ContentPart?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)
}

object GeminiFinancialAdvisor {
    suspend fun getFinancialTips(expensesSummary: String, budgetsSummary: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Sincroniza tu clave API en AI Studio para recibir consejos financieros inteligentes de IA."
        }

        val prompt = """
            Eres un Planificador Financiero Personal y Asesor de Presupuesto con estilo iOS Siri, minimalista, profesional y claro.
            Analiza la siguiente información de gastos y presupuestos mensuales, y provee 3 consejos de ahorro ultra-cortos, accionables, optimistas y con estilo profesional.
            
            Información de gastos actuales:
            $expensesSummary
            
            Información de presupuestos definidos:
            $budgetsSummary
            
            Formato requerido:
            - Mantén los textos sumamente breves (máximo 2 líneas por consejo, estilo notificación inteligente de iOS).
            - No uses asteriscos redundantes o excesivo formateo. Usa un tono que empodere al usuario. Provee respuestas en Español.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                ContentPart(
                    parts = listOf(TextPart(text = prompt))
                )
            )
        )

        return try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No fue posible generar consejos en este momento. Revisa tu presupuesto."
        } catch (e: Exception) {
            "Análisis rápido: Intenta registrar más gastos para que la inteligencia artificial personalice tus recomendaciones de ahorro."
        }
    }
}
