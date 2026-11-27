package com.example.educationaladministrationsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educationaladministrationsystem.processLogic.EASNotice
import com.example.educationaladministrationsystem.processLogic.MainViewModel
import com.example.educationaladministrationsystem.processLogic.classTable.NewVersionUpCheck
import com.example.educationaladministrationsystem.width.HoldWidth
import com.example.educationaladministrationsystem.width.eASWeb.EASWeb
import com.example.educationaladministrationsystem.width.welcome.FirstPageWelcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val appStore= CoroutineScope(SupervisorJob()+ Dispatchers.IO)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EASNotice.createNoticeChannel(this)//注册通知Channel
        NewVersionUpCheck.checkIfNew(this){//新版本更新重新注册提醒
            appStore.launch { NewVersionUpCheck.onHandleClockRegister(this@MainActivity) }
        }
        setContent {
            val viewModel: MainViewModel= viewModel()
            val isFirst by viewModel.isFirst.collectAsStateWithLifecycle()
            val isLoading by viewModel.isLoading.collectAsState()
            if (isLoading){
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                }
            }else{
                when(isFirst){
                    false->{HoldWidth().HoldLoad()}
                    true->{
                        FirstPageWelcome().InitFirstPage({viewModel.finishAllAndNavigate()})
                    }
                }
            }

        }
        EASWeb().webInitOnMain(this)//初始化webview
    }
}

