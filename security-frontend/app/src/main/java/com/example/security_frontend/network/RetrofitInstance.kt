import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.security_frontend.network.ApiService

/*
 * RetofitClient: Central service for creating API instances.
 * Uses 10.0.2.2 to connect to the Spring Boot backend running on the host machine.
 */

// WARNING: Use 10.0.2.2 for the Android emulator. Do NOT use "localhost" or "127.0.0.1".
private const val BASE_URL = "http://10.0.2.2:8080/api/"

object RetrofitInstance {

    val api: ApiService by lazy {

        // 1. Create a Logging Interceptor for debugging network issues
        val logging = HttpLoggingInterceptor().apply {
            // Set level to BODY to log headers, body, request/response details
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        // 2. Create the OkHttpClient and add the interceptor
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS) // Increase timeout for debugging
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        // 3. Create Retrofit instance using the custom client
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Use the custom client with logging
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
