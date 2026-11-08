package com.example.educationaladministrationsystem.processLogic.settings.versionUpload


import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import java.net.HttpURLConnection
import java.net.URL

interface ApiService {
    @GET
    suspend fun getVersionUpload(): GitHubRelease

}
object Api{
    private const val url="https://api.github.com/repos/chinaqianb/EducationalAdministrationSystemApp/releases/latest"
   private val json =Json{
        ignoreUnknownKeys=true//忽略json中多余的字段
        coerceInputValues=true//空值使用默认值
        isLenient=true//宽松解析
    }
    private val retrofit= Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
    val service: ApiService=retrofit.create(ApiService::class.java)

}