package com.example.cyclapp.data

import com.example.cyclapp.model.NewsResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("everything")
    suspend fun getRecyclingNews(
        @Query("q") query: String = "reciclaje OR recycling",
        @Query("language") language: String = "es",
        @Query("apiKey") apiKey: String
    ): NewsResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://newsapi.org/v2/"

    val newsApiService: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }
}