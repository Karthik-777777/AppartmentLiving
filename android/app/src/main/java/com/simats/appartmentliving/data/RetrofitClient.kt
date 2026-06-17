package com.simats.appartmentliving.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://apartment-living-backend.onrender.com/"

    @Volatile
    var token: String? = null

    private var appContext: android.content.Context? = null

    fun init(context: android.content.Context) {
        appContext = context.applicationContext
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                token?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }
                val request = requestBuilder.build()
                
                // Log request details
                val requestBodyString = try {
                    val buffer = okio.Buffer()
                    request.body()?.writeTo(buffer)
                    buffer.readUtf8()
                } catch (e: Exception) {
                    "Error reading body"
                }
                android.util.Log.d("RetrofitClient", "--> ${request.method()} ${request.url()}\nHeaders: ${request.headers()}Body: $requestBodyString\n--> END")

                val response = chain.proceed(request)
                
                // Clear session on 401 Unauthorized (token expired)
                if (response.code() == 401) {
                    appContext?.let { context ->
                        AuthManager(context).clearSession()
                    }
                }

                // Log response details
                val responseBody = response.body()
                val responseBodyString = try {
                    if (responseBody != null) {
                        val source = responseBody.source()
                        source.request(Long.MAX_VALUE)
                        val buffer = source.buffer()
                        buffer.clone().readUtf8()
                    } else {
                        "No body"
                    }
                } catch (e: Exception) {
                    "Error reading body: ${e.message}"
                }
                android.util.Log.d("RetrofitClient", "<-- ${response.code()} ${request.url()}\nBody: $responseBodyString\n<-- END")
                
                response
            }
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
