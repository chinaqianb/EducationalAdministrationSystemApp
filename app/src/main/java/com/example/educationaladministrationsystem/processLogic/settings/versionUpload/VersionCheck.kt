package com.example.educationaladministrationsystem.processLogic.settings.versionUpload


import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET

interface VersionApiService {
    @GET("repos/chinaqianb/EducationalAdministrationSystemApp/releases/latest")
    suspend fun getVersionUpload(): GitHubRelease
    @GET("users/chinaqianb")
    suspend fun getAuthor(): GithubPersonData

}
object VersionApi{
    private const val url="https://api.github.com/"
   private val json =Json{
        ignoreUnknownKeys=true//忽略json中多余的字段
        coerceInputValues=true//空值使用默认值
        isLenient=true//宽松解析
    }
    private val retrofit= Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
    val service: VersionApiService=retrofit.create(VersionApiService::class.java)

}