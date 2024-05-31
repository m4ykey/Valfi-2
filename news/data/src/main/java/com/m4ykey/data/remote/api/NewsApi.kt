package com.m4ykey.data.remote.api

import com.m4ykey.core.Constants.DOMAINS
import com.m4ykey.core.Constants.PAGE_SIZE
import com.m4ykey.core.Keys.NEWS_API_KEY
import com.m4ykey.data.remote.model.NewsDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("v2/everything")
    suspend fun getMusicNews(
        @Query("q") query : String = "music",
        @Query("pageSize") pageSize : Int = PAGE_SIZE,
        @Query("page") page : Int = 1,
        @Query("sortBy") sortBy : String = "publishedAt",
        @Query("domains") domains : String = DOMAINS,
        @Query("apiKey") apiKey : String = NEWS_API_KEY
    ) : NewsDto

}