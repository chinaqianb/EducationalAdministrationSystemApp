package com.example.educationaladministrationsystem.width.eASWeb

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educationaladministrationsystem.processLogic.eASWeb.EASViewModel

class EASWeb {
    @Composable
    fun InitEAS(paddingValues: PaddingValues,jump:Boolean=false){

            LoadWebView(paddingValues = paddingValues, jump = jump)

    }
    @Composable
    private fun LoadWebView(viewModel: EASViewModel= viewModel(),paddingValues: PaddingValues,jump: Boolean=false){
        val loading by viewModel.loading.collectAsStateWithLifecycle()
        var webView by remember { mutableStateOf<WebView?>(null) }
       val canGoBack by viewModel.canBack.collectAsState()
        //返回操作
        BackHandler(enabled = canGoBack) {
            webView?.goBack()
        }
//        DisposableEffect(Unit){
//
//                onDispose {
//                    webView?.let {wv->
//                    wv.stopLoading()
//                    wv.destroy()
//                }
//            }
//        }
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                AndroidView(
                    factory = { context ->
                        viewModel.getOrCreateWebView(context).apply {
                            webViewClient = viewModel.webClient
                            settings.javaScriptEnabled = true
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                        }.also { webView = it }
                    },
                    onRelease = { release -> viewModel.saveEASView { release.saveState(it) };release.stopLoading() },
                    modifier = WebViewModifier()
                )
            if (loading){
                //加载时
                Box(modifier = Modifier.matchParentSize(), contentAlignment =Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

        }
        LaunchedEffect(webView){
            webView?.let { wv->
                viewModel.initPage({ wv.restoreState(it) != null }, { url->wv.loadUrl(url) }, jump)
            }
        }

    }

    private fun WebViewModifier(): Modifier{
        return Modifier.fillMaxSize()
    }
    fun webInitOnMain(context: Context){
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                WebView(context).apply {
                    loadUrl("about:blank")
                    destroy()
                }
            } catch (e: Throwable) {
                Log.w("WebViewPreload", "preload failed", e)
            }
        }, 800)

    }
}