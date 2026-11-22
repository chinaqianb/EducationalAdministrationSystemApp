package com.example.educationaladministrationsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.educationaladministrationsystem.processLogic.EASNotice
import com.example.educationaladministrationsystem.ui.theme.EducationalAdministrationSystemTheme
import com.example.educationaladministrationsystem.width.HoldWidth
import com.example.educationaladministrationsystem.width.eASWeb.EASWeb

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EASNotice.createNoticeChannel(this)//注册通知Channel
        setContent {
            HoldWidth().HoldLoad()
        }
    }
}

