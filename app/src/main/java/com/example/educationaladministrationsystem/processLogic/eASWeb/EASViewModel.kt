package com.example.educationaladministrationsystem.processLogic.eASWeb

import android.app.Application
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.educationaladministrationsystem.processLogic.DataSave
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EASViewModel(application: Application): AndroidViewModel(application){
    private val _loading= MutableStateFlow(false)
    val loading= _loading.asStateFlow()
    private val _webSaveBundle=MutableStateFlow(Bundle())
    private var firstLoad=false
    data class ThreeData(val name: String,val password: String,val url: String)
    private var initData= ThreeData("","","")
    private var firstInject: String?=null
    val webClient= object :WebViewClient(){
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            _loading.value=true//开始加载
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            _loading.value=false//结束加载
            handleJsPageInject(view,url)


        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            if (request?.isForMainFrame==true){
                _loading.value=false
            }
        }

    }
    fun initPage(restore:(Bundle)-> Unit,loadUrl:(Unit)-> Unit,jump: Boolean) {
        if (firstLoad&&!jump) { return}
        if (jump){firstInject=null}
        firstLoad=true
        _loading.value=true
        viewModelScope.launch {
            val save = DataSave(context = application.applicationContext).read()
            initData = ThreeData(save["user_name"] ?: "", save["password"] ?: "", save["login_url"] ?: "")


            if (_webSaveBundle.value.containsKey("WEB_EAS_STATE")) {
                restore(_webSaveBundle.value.getBundle("WEB_EAS_STATE")!!)
            } else {
                loadUrl(Unit)
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
    private fun handleJsPageInject(view: WebView?,url: String?){
        if (view==null||url==null)return
        val nowPage=url.substringBefore('?').substringBefore('#')
        val isLoginPage=nowPage.endsWith(".html",ignoreCase = true)
        if (!isLoginPage){//不是登录页
            Log.d("EAS","no_login")
            firstInject=null
            return
        }
        if (nowPage==firstInject){//同一个界面第二次
            Log.d("EAS","second")
            return
        }
        firstInject=nowPage//第一次登录
        view.postDelayed({
            view.evaluateJavascript(injectJsWork(initData.name,initData.password),null)
        },1000)


    }
    private fun injectJsWork(name: String,password: String): String{
        val js ="""
            (function () {
                const nameInput = document.getElementById('yhm');
                const passwordInput = document.getElementById('mm');
                const loginButton = document.getElementById('dl');
                if (/\.html$/i.test(location.pathname)&&nameInput&&passwordInput&&loginButton){
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