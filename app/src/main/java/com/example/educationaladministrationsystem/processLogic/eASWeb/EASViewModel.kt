package com.example.educationaladministrationsystem.processLogic.eASWeb

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EASViewModel: ViewModel() {
    private val _loading= MutableStateFlow(false)
    val loading= _loading.asStateFlow()
    private val _webSaveBundle=MutableStateFlow(Bundle())
    private var firstLoad=false
    val webClient= object :WebViewClient(){
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            _loading.value=true//开始加载
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            _loading.value=false//结束加载
            //注入js
            view?.postDelayed({
                view.evaluateJavascript(injectJsWork("2025","ing"),null)
            },1000)
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            if (request?.isForMainFrame==true){
                _loading.value=false
            }
        }

    }
    fun initPage(restore:(Bundle)-> Unit,loadUrl:(Unit)-> Unit,jump: Boolean) {
        if (firstLoad&&!jump) {Log.d("aaa","返回?"); return}
        firstLoad=true
        _loading.value=true
        if (_webSaveBundle.value.containsKey("WEB_EAS_STATE")){
            restore(_webSaveBundle.value.getBundle("WEB_EAS_STATE")!!)
        }else{
            loadUrl(Unit)
        }

    }
    fun saveEASView(saveDate:(Bundle)-> Unit){
        val bundle= Bundle()
        saveDate(bundle)
       _webSaveBundle.update { cutten->
           Bundle(cutten).apply { putBundle("WEB_EAS_STATE",bundle) }
       }

    }
    fun injectJsWork(name: String,password: String): String{
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