package com.example.educationaladministrationsystem.width.eASWeb

import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
        DisposableEffect(Unit){

                onDispose {
                    webView?.let {wv->
                    wv.stopLoading()
                    wv.destroy()
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = viewModel.webClient
                            settings.javaScriptEnabled = true
                            viewModel.initPage({ restoreState(it) != null }, { url->loadUrl(url) }, jump)
                        }.also { webView = it }
                    },
                    onRelease = { release -> viewModel.saveEASView { release.saveState(it) } },
                    modifier = WebViewModifier()
                )
            if (loading){
                //加载时
                Box(modifier = Modifier.matchParentSize(), contentAlignment =Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

        }

    }

    private fun WebViewModifier(): Modifier{
        return Modifier.fillMaxSize()
    }
}