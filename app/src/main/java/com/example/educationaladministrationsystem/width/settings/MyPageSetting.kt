package com.example.educationaladministrationsystem.width.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educationaladministrationsystem.processLogic.settings.SettingsViewModel
import com.composables.icons.lucide.R.drawable as Lucide

class MyPageSetting {
    @Composable
    fun InitMyPage(paddingValues: PaddingValues,viewModel: SettingsViewModel.MyPageSettingViewModel= viewModel()){
        val imageUri by viewModel.imageUri.collectAsState()
        val userName by viewModel.userName.collectAsState()
        val loading by viewModel.loading.collectAsState()
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                viewModel.imageSaveToData(uri)
            }
        }
        val onChoose = remember(launcher) {
            {
                launcher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        }
        Column(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF6F6F6)),
            verticalArrangement = Arrangement.Top
        ){
            if (loading){
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp))//转圈动画
                }
            }else{
                PictureAndNameSetting(viewModel, onChoose,userName)
            }
        }
    }
    @Composable
    private fun PictureAndNameSetting(viewModel: SettingsViewModel.MyPageSettingViewModel,onChoose:()-> Unit,userName: String){
        Settings().RenderSettingBoxItem("个人设置", content = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically){
                Text("更改个人头像")
                IconButton(onClick = {onChoose()}){
                    Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_images), contentDescription = "选择图片")
                }
            }
            TextField(value = userName,
                onValueChange = {viewModel.changeUserName(it)},
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {viewModel.userNameToData()})
                    { Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_check), contentDescription = "完成") }
                }
            )
        })
    }
    @Composable
    private fun chooseImage(viewModel: SettingsViewModel.MyPageSettingViewModel): () -> Unit{
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                viewModel.imageSaveToData(uri)
            }
        }
        return {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }
}