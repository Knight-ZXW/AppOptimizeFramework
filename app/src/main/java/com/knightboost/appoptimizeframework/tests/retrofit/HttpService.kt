package com.knightboost.appoptimizeframework.tests.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.nio.file.attribute.AclEntry.newBuilder

object HttpService {
    val retrofit:Retrofit
    init {
         retrofit = Retrofit.Builder().client(
            OkHttpClient.Builder()
                .build()
        ).baseUrl("http://api.bilibili.com/").build()
    }

}