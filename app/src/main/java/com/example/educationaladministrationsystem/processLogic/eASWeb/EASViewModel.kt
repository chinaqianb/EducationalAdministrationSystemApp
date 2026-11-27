package com.example.educationaladministrationsystem.processLogic.eASWeb

import android.app.Application
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.educationaladministrationsystem.processLogic.DataSave
import com.example.educationaladministrationsystem.processLogic.classTable.ClassTableApi
import com.example.educationaladministrationsystem.processLogic.classTable.ClassTableClock
import com.example.educationaladministrationsystem.processLogic.eASWeb.AEScrypt.PasswordCrypt.decryptPassword
import com.example.educationaladministrationsystem.processLogic.eASWeb.AEScrypt.decryptAES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.toLongOrDefault

class EASViewModel(application: Application): AndroidViewModel(application){
    private val _loading= MutableStateFlow(false)
    val loading= _loading.asStateFlow()
    private val _webSaveBundle=MutableStateFlow(Bundle())
    private var firstLoad=false
    data class ThreeData(val name: String,val password: String,val url: String)
    private var initData= ThreeData("","","")
    private var isJsInjectOn =false
    private var firstInject: String?=null
    private val _canBack =MutableStateFlow(false)
    val canBack=_canBack.asStateFlow()
    private var lastSyncedUrl: String? = null

    val webClient= object :WebViewClient(){
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            _loading.value=true//开始加载
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            _loading.value=false//结束加载
            if (url == null || url == lastSyncedUrl) return
            val thisCookies=handleJsPageInject(view,url)?:return
            if (thisCookies.jsId.isEmpty()||thisCookies.route.isEmpty())return
            lastSyncedUrl=url
                Log.d("eas","开始同步cookies:${thisCookies}")
                ClassTableApi.cookieJar.syncCookies(url.orEmpty(),thisCookies)
                //登录成功后强制获取一次数据
                viewModelScope.launch {
                    ClassTableClock.getClassTableList(application.applicationContext)
                }

        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            if (request?.isForMainFrame==true){
                _loading.value=false
            }
        }

        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
            _canBack.value=view?.canGoBack()==true
        }

    }
    fun initPage(restore:(Bundle)-> Unit, loadUrl:(String)-> Unit, jump: Boolean) {
        if (firstLoad&&!jump) { return}
        if (jump){firstInject=null}
        firstLoad=true
        _loading.value=true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val dataSave = DataSave(context = application.applicationContext)
                val save = dataSave.read(dataSave.LOGIN_SETTING_NAME)
                initData = ThreeData(
                    (save["user_name"] ?: "").decryptAES(),
                    (save["password"] ?: "").decryptPassword(),
                    save["login_url"] ?: ""
                )

                isJsInjectOn = save.getOrDefault("js_inject", "") == "1"
            }
            if (_webSaveBundle.value.containsKey("WEB_EAS_STATE")) {
                restore(_webSaveBundle.value.getBundle("WEB_EAS_STATE")!!)
            } else {
                loadUrl(initData.url)
            }
        }

    }
    fun saveEASView(saveDate:(Bundle)-> Unit){
        val bundle= Bundle()
        saveDate(bundle)
       _webSaveBundle.update { cutten->
           Bundle(cutten).apply { putBundle("WEB_EAS_STATE",bundle) }
       }

    }
    private fun handleJsPageInject(view: WebView?,url: String?): EasCookies?{
        if (view==null||url==null)return null
        val nowPage=url.substringBefore('?').substringBefore('#')
        val isLoginPage=nowPage.endsWith("slogin.html",ignoreCase = true)
        if (!isLoginPage){//不是登录页
            Log.d("EAS","no_login")
            firstInject=null
            return checkAndGetCookies(url)
        }
        if (nowPage==firstInject){//同一个界面第二次
            Log.d("EAS","second")
            return null
        }
        firstInject=nowPage//第一次登录
        if (isJsInjectOn) {
            view.postDelayed({
                view.evaluateJavascript(injectJsWork(initData.name, initData.password), null)
            }, 1000)
        }
        return null
    }
    private suspend fun getEasCookies(data: DataSave,url: String): EasCookies{
            var result: EasCookies
            val map = data.read(data.EAS_COOKIES)
            val time = map["time"]
            result = EasCookies(map.getOrDefault("js_id", ""), map.getOrDefault("route", ""))
            if (result.jsId.isEmpty() || result.route.isEmpty() || time == null || System.currentTimeMillis() - time.toLongOrDefault(
                    0
                ) >= 1000L * 60 * 1
            ) {
                //过期或者是空的，重新获取
                result = checkAndGetCookies(url)
                if (result.jsId.isEmpty()||result.route.isEmpty())return result
                data.save(
                    mapOf(
                        "js_id" to result.jsId,
                        "route" to result.route,
                        "time" to System.currentTimeMillis().toString()
                    ),data.EAS_COOKIES
                )
            }
        return result
    }
    private fun checkAndGetCookies(url: String): EasCookies{
            val cookies = CookieManager.getInstance().getCookie(url)
            val easCookies = patternCookies(cookies)
            Log.d("EAS", "cookies:${cookies}")
        return easCookies
    }
    data class EasCookies(val jsId: String,val route: String)
    private fun patternCookies(cookies: String): EasCookies{
        val jsessionid = Regex("""JSESSIONID=([^;\s]+)""")
            .find(cookies)?.groupValues?.get(1)

        val route = Regex("""(?:^|;\s*)route=([^;\s]+)""")
            .find(cookies)?.groupValues?.get(1)

        return EasCookies(jsessionid.orEmpty(),route.orEmpty())
    }
    private fun injectJsWork(name: String,password: String): String{
        val js ="""
            (function () {
                const nameInput = document.getElementById('yhm');
                const passwordInput = document.getElementById('mm');
                const loginButton = document.getElementById('dl');
                if (/slogin\.html$/i.test(location.pathname)&&nameInput&&passwordInput&&loginButton){
                    nameInput.value='${name}';
                    passwordInput.value='${password}';
//                    loginButton.click();
                }
            })();
        """.trimIndent()
        return js
    }
    private fun getJs(): String{
        return TODO()
    }
}