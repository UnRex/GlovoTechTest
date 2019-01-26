

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient{
    private const val BASE_URL = "http://192.168.1.128:3000/"

    private lateinit var apiService: ApiService

    init {
        val retrofit = initRetrofit()
        apiService = retrofit.create(ApiService::class.java)
    }

    fun getService() = apiService

    private fun initRetrofit(): Retrofit{

        val client = OkHttpClient.Builder().apply {
            networkInterceptors().add(Interceptor {chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .method(original.method(), original.body())
                    .build()
                chain.proceed(request)
            })

            //add interceptor if needed

        }

        return Retrofit.Builder().baseUrl(BASE_URL)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .build()
    }
}