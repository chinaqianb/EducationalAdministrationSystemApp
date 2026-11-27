package com.example.educationaladministrationsystem.processLogic.classTable

import android.util.Log
import com.example.educationaladministrationsystem.processLogic.eASWeb.EASViewModel
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import retrofit2.http.Headers

interface ClassTableApiService{
    @Headers(
        "Referer: https://jwx.gzmtu.edu.cn/jwglxt/kbcx/xskbcxMobile_cxTimeTableIndex.html",
        "User-Agent: Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36",
        "X-Requested-With: XMLHttpRequest"
    )
    @GET("xskbcxMobile_cxTimeTableIndex.html")
    suspend fun getPage(): String
    @FormUrlEncoded
    @Headers(
        "Referer: https://jwx.gzmtu.edu.cn/jwglxt/kbcx/xskbcxMobile_cxTimeTableIndex.html",
        "User-Agent: Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36",
        "X-Requested-With: XMLHttpRequest"
    )
    @POST("xskbcxMobile_cxXsgrkb.html")
    suspend fun getClassTable(
        @Field("xnm")xnm: String,
        @Field("xqm")xqm: String,
        @Field("zs")zs: String,
        @Field("kblx")kblx: String="1",
        @Field("doType")doType: String="app",
    ): ScheduleResponse
    @FormUrlEncoded
    @Headers(
        "Referer: https://jwx.gzmtu.edu.cn/jwglxt/kbcx/xskbcxMobile_cxTimeTableIndex.html",
        "User-Agent: Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36",
        "X-Requested-With: XMLHttpRequest"
    )
    @POST("xskbcxMobile_cxZc.html")
    suspend fun getTermWeek(
        @Field("xnm")xnm: String,
        @Field("xqm")xqm: String//上学期为3,下学期为12
    ): List<ResponseWeekData>
}
object ClassTableApi{
    private val url="https://jwx.gzmtu.edu.cn/jwglxt/kbcx/"
    private val json =Json{
        ignoreUnknownKeys=true//忽略json中多余的字段
        coerceInputValues=true//空值使用默认值
        isLenient=true//宽松解析
    }
    val cookieJar= EasCookiesJar()
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
//        .addInterceptor(LogInterceptor())
        .build()

    private val retrofit= Retrofit.Builder()
        .baseUrl(url)
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
    val service: ClassTableApiService=retrofit.create(ClassTableApiService::class.java)
}
class EasCookiesJar: CookieJar{
    private val cookieStore=mutableMapOf<String, MutableList<Cookie>>()
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return
        val list = cookieStore.getOrPut(url.host) { mutableListOf() }
        for (c in cookies) {
            list.removeAll { it.name == c.name && it.path == c.path }
            list.add(c)
        }

    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val list = cookieStore[url.host] ?: emptyList()
        val filtered = list.filter { it.matches(url) }
        return filtered
    }
    fun syncCookies(url: String,cookies: EASViewModel.EasCookies){
        val httpUrl=url.toHttpUrlOrNull()?:return
        val host =httpUrl.host
        cookieStore[host]=mutableListOf(
            //cookies:_path=/jwglxt; JSESSIONID=4B06B1231A61BACB69CA22A642658EE1; route=645b46496d07bf7bd22eaa1e32bb752d
            Cookie.Builder().hostOnlyDomain(host).path("/").name("JSESSIONID").value(cookies.jsId).build(),
            Cookie.Builder().hostOnlyDomain(host).path("/").name("route").value(cookies.route).build()
        )
        Log.d("cookiesjar","cookies同步成功")
    }
    fun isEmpty(): Boolean{
       val host="jwx.gzmtu.edu.cn"
       return cookieStore[host].isNullOrEmpty()
    }
}
class LogInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val req = chain.request()
        val resp = chain.proceed(req)

        Log.w("HTTP", "→ ${req.method} ${req.url}")
        Log.w("HTTP", "  Cookie: ${resp.request.headers("Cookie")}")
        Log.w("HTTP", "  Referer: ${req.header("Referer")}")
        Log.w("HTTP", "  User-Agent: ${req.header("User-Agent")}")
        Log.w("HTTP", "← code=${resp.code}")
        Log.w("HTTP", "  Content-Type: ${resp.header("Content-Type")}")
        Log.w("HTTP", "  Location: ${resp.header("Location")}")
        Log.w("HTTP", "  finalUrl: ${resp.request.url}")

        return resp
    }
}