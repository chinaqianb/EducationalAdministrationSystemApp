package com.example.educationaladministrationsystem.width.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educationaladministrationsystem.processLogic.settings.SettingsViewModel
import java.io.File
import com.composables.icons.lucide.R.drawable as Lucide
class VersionInfoSetting {

    @Composable
    fun InitVersionInfoSetting(paddingValues: PaddingValues,viewModel: SettingsViewModel.VersionSettingViewModel= viewModel()){
        val context= LocalContext.current

        var showDialog by remember{ mutableStateOf(false) }
        var dialogTitle by remember { mutableStateOf("") }
        var dialogMessage by remember { mutableStateOf("") }
        var onClickEvent by remember { mutableStateOf<(() -> Unit)?>(null)}
        val settingWait by viewModel.toSettingPage.collectAsState()
        var downloadFile by remember { mutableStateOf(File("")) }
        LaunchedEffect(Unit){
            viewModel.noNeedUpdate.collect {
                dialogTitle="已经是最新版本"
                dialogMessage="未发现新版本"
                onClickEvent={showDialog=false}
                showDialog=true
                Log.d("version_setting","no_need_emit")
            }
        }
        LaunchedEffect(Unit){
            viewModel.needUpdate.collect {
                dialogTitle="更新新版本"
                dialogMessage="发现新的版本${it.tag},下载地址${it.url}"
                onClickEvent={viewModel.downNewVersion(SettingsViewModel.VersionSettingViewModel.DownloadInf(it.tag,it.url,it.fileHash),context)}
                showDialog=true
                Log.d("version_setting","need_emit")
            }
        }
        LaunchedEffect(Unit){
            viewModel.hasError.collect { Toast.makeText(context,"错误by:${it}", Toast.LENGTH_SHORT) }
        }
        LaunchedEffect(Unit){
            viewModel.apkReady.collect {
                dialogTitle="安装新版本"
                dialogMessage="安装包${it.name}下载完成,是否安装?"
                downloadFile=it
                onClickEvent={
                    if (context.packageManager.canRequestPackageInstalls()){
                        viewModel.installAPK(context,it)
                    }else{
                        val intent= Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                            data= Uri.parse("package:${context.packageName}")
                        }
                        viewModel.setToSetting(true)
                        context.startActivity(intent)
                    }
                }
            }
        }
        LifecycleResumeEffect(Unit){
            if (settingWait){
                if (context.packageManager.canRequestPackageInstalls()){
                    viewModel.setToSetting(false)
                    viewModel.installAPK(context,downloadFile)
                }
            }
            onPauseOrDispose {  }
        }
        if (showDialog) {
            RenderDialog({showDialog=false},dialogTitle,dialogMessage,{onClickEvent?.invoke()})
        }
        Column(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF6F6F6)),
            verticalArrangement = Arrangement.Top
        ){
            RenderVersionComponent(context,viewModel)
        }
    }
    @Composable
    fun RenderVersionComponent(context: Context, viewModel: SettingsViewModel.VersionSettingViewModel){
        val nowVersion=viewModel.getVersion(context)
        Settings().RenderSettingBoxItem("版本信息",{

            Text("当前版本:${nowVersion}")
            IconButton(
                onClick = {viewModel.checkVersionUpdate(context)}
            ){ Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_send), contentDescription = "检查更新") }

        })
    }
    @Composable
    fun RenderNewVersionDownloadItem(){

    }
    @Composable
    private fun RenderDialog(onClose:()-> Unit,title: String,des: String,onClick:()-> Unit){
        AlertDialog(
            onDismissRequest = {onClose()},
            title ={Text(title)},
            text={Text(des)},
            confirmButton = { TextButton(onClick ={ onClick() }){Text("确认")} },
            dismissButton = {TextButton(onClick = {onClose()}){Text("取消")} }
        )
    }
}