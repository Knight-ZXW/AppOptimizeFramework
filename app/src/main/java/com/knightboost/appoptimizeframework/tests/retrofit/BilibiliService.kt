package com.knightboost.appoptimizeframework.tests.retrofit

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BilibiliService {

    @GET("x/web-interface/archive/stat")
    fun archiveStat(@Query("aid") aid:Long):Call<ResponseBody>

    @GET("x/web-interface/archive/stat")
    fun archiveStat2(@Query("aid") aid:Long):Call<ResponseBody>
}