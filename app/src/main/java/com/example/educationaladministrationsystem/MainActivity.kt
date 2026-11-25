package com.example.educationaladministrationsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educationaladministrationsystem.processLogic.EASNotice
import com.example.educationaladministrationsystem.processLogic.MainViewModel
import com.example.educationaladministrationsystem.ui.theme.EducationalAdministrationSystemTheme
import com.example.educationaladministrationsystem.width.HoldWidth
import com.example.educationaladministrationsystem.width.eASWeb.EASWeb
import com.example.educationaladministrationsystem.width.welcome.FirstPageWelcome

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EASNotice.createNoticeChannel(this)//注册通知Channel
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
    }
}

