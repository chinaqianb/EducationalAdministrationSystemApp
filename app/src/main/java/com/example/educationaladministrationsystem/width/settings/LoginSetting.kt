package com.example.educationaladministrationsystem.width.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educationaladministrationsystem.processLogic.settings.SettingsViewModel
import com.composables.icons.lucide.R.drawable as Lucide
class LoginSetting {
    @Composable
    fun InitLoginSetting(paddingValues: PaddingValues,viewModel: SettingsViewModel.LoginSettingViewModel= viewModel()){
        val user by viewModel.userName.collectAsState()
        val password by viewModel.password.collectAsState()
        val url by viewModel.loginUrl.collectAsState()
        val context= LocalContext.current
        LaunchedEffect(Unit){
            viewModel.changeSuccess.collect { Toast.makeText(context,"成功修改",Toast.LENGTH_SHORT).show() }
        }
        LaunchedEffect(Unit){
            viewModel.changeError.collect { Toast.makeText(context,"修改失败,错误代码:${it}", Toast.LENGTH_SHORT).show() }
        }
        Column(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF6F6F6)),
            verticalArrangement = Arrangement.Top
        ){
            NameAndPasswordSetting(user, password,viewModel)
            Spacer(Modifier.height(10.dp))
            UrlSetting(url,viewModel)
        }
    }
    @Composable
    private fun NameAndPasswordSetting(user: String,password: String,viewModel: SettingsViewModel.LoginSettingViewModel){

        Settings().RenderSettingBoxItem("学号和密码设置"){
            TextField(value = user,
                onValueChange = {viewModel.someDataChange(user=it)},
                label = { Text("学号") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {viewModel.changeData()})
                    { Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_check), contentDescription = "完成") }
                }
            )
            Spacer(modifier = Modifier.height(5.dp))
            TextField(value = password,
                onValueChange = {viewModel.someDataChange(password = it)},
                label = {Text("密码")},
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {viewModel.changeData()})
                    { Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_check), contentDescription = "完成") }
                    }
                )
        }
    }
    @Composable
    private fun UrlSetting(url: String,viewModel: SettingsViewModel.LoginSettingViewModel){
        Settings().RenderSettingBoxItem("登录网址设置"){
            TextField(value = url,
                onValueChange = {viewModel.someDataChange(url = it)},
                label = {Text("网址")},
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                IconButton(onClick = {viewModel.changeData()})
                { Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_check), contentDescription = "完成") }

                }
            )
        }
    }
}