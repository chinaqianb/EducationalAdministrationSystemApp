package com.example.educationaladministrationsystem.width

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.educationaladministrationsystem.processLogic.aboutUs.AboutUsViewModel
import com.example.educationaladministrationsystem.width.settings.Settings

class AboutUs {
    @Composable
    fun InitAboutUs(paddingValues: PaddingValues,viewModel: AboutUsViewModel= viewModel()) {
        viewModel.getAuthor()
        val isLoading by viewModel.isLoading.collectAsState()
       val isGet by viewModel.isGet.collectAsState()
        Column(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF6F6F6)),
            verticalArrangement = Arrangement.Top
        ){
          if (isLoading){
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                  CircularProgressIndicator(modifier = Modifier.size(30.dp))//转圈动画
              }
          }else{
              RenderAboutUs(isGet)
          }
        }
    }
    @Composable
    private fun RenderAboutUs(data: AboutUsViewModel.AuthorInfo){
        Settings().RenderSettingBoxItem("关于我们", {
            SubcomposeAsyncImage(
                data.authorImage, contentDescription = "作者头像",
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(30.dp))//转圈动画
                    }
                }, error = {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text("加载失败")
                    }
                })
            Text(data.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        })
    }
}