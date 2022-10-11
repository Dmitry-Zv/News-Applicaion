package by.zharikov.newsapplicaion.api

import by.zharikov.newsapplicaion.BuildConfig
import by.zharikov.newsapplicaion.utils.Constants
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitNews {
    private var retrofit: Retrofit? = null

    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val client = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor { chain ->
                    var request = chain.request()
                    var newRequest = request.newBuilder().header("Authorization", Constants.API_KEY)
                    chain.proceed(newRequest.build())
                }
                addInterceptor(interceptor)
                pingInterval(1, TimeUnit.SECONDS)
            }
        }
        .build()


    fun getApi(): NewsApi {


        retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory.invoke())
            .client(client)
            .build()


        return retrofit!!.create(NewsApi::class.java)
    }

}