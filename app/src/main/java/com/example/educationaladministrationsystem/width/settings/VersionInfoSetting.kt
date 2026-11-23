package com.example.educationaladministrationsystem.width.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educationaladministrationsystem.processLogic.aboutUs.AboutUsViewModel
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
        val newVersionInf by viewModel.newVersionInf.collectAsState()
        val hasNewVersion by viewModel.hasNewVersion.collectAsState()
        val hasReadyApk by viewModel.hasReadyApk.collectAsState()
        val isOnDownload by viewModel.isOnDownload.collectAsState()
        val downloadProgress by viewModel.downloadProgress.collectAsState()
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
                onClickEvent={//viewModel.downNewVersion(SettingsViewModel.DownloadInf(it.tag,it.url,it.fileHash),context);
                    showDialog=false}
                showDialog=true
                Log.d("version_setting","need_emit")
            }
        }
        LaunchedEffect(Unit){
            viewModel.hasError.collect { Toast.makeText(context,"错误by:${it}", Toast.LENGTH_SHORT) }
        }
        LaunchedEffect(Unit){
            viewModel.apkReady.collect {
                Log.d("version_setting","new_version_ard")
//                dialogTitle="安装新版本"
//                dialogMessage="安装包${it.name}下载完成,是否安装?"
//                downloadFile=it
//                onClickEvent={
//                    if (context.packageManager.canRequestPackageInstalls()){
//                        Log.d("version_setting","can go")
//                        viewModel.installAPK(context,it)
//                    }else{
//                        Log.d("version_setting","no can")
//                        val intent= Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
//                            data= Uri.parse("package:${context.packageName}")
//                        }
//                        viewModel.setToSetting(true)
//                        context.startActivity(intent)
//                    }
//                }
//                showDialog=true
            }
        }

        if (showDialog) {
            RenderDialog({showDialog=false},dialogTitle,dialogMessage,{onClickEvent?.invoke()})
        }
        Column(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF6F6F6)),
            verticalArrangement = Arrangement.Top
        ){
            RenderVersionComponent(context,viewModel)
            Spacer(Modifier.height(10.dp))
            if (!viewModel.isMyVersionTheNew(context)) {
                RenderVersionNew(
                    downloadProgress,
                    downloadFile,
                    isOnDownload,
                    newVersionInf,
                    hasNewVersion,
                    hasReadyApk,
                    context,
                    viewModel
                )
            }
        }
    }
    @Composable
    private fun RenderVersionNew(downloadProgress: Int,file: File,isOnDownload: Boolean, versionInf: SettingsViewModel.DownloadInf, hasVersion: Boolean, hasApk: Boolean, context: Context, viewModel: SettingsViewModel.VersionSettingViewModel){
        if (hasVersion){
            //有新版本时渲染
            if (!hasApk){
                //下载按钮
                RenderNewVersionDownloadItem(downloadProgress,isOnDownload, context,versionInf,viewModel)
            }else{
                //安装包就绪时，安装按钮
                RenderNewVersionDownloadInstall(file,context,versionInf,viewModel)
            }

        }
    }
    @Composable
    private fun RenderVersionComponent(context: Context, viewModel: SettingsViewModel.VersionSettingViewModel){
        val nowVersion=viewModel.getVersion(context)
        val isOnCheck by viewModel.isOnCheck.collectAsStateWithLifecycle()
        val infiniteTransition = rememberInfiniteTransition(label = "refresh")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "refreshAngle"
        )
        Settings().RenderSettingBoxItem("版本信息", content = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                Text("当前版本:${nowVersion}")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically){
                Text("检查更新")
                IconButton(
                    onClick = {viewModel.checkVersionUpdate(context)}
                ){ Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_refresh_ccw), contentDescription = "检查更新",
                    modifier = Modifier.rotate(if (isOnCheck)angle else 0f))}
            }

            //Button(onClick = {viewModel.handEmit()}){Text("手动触发")}

        })
    }
    @Composable
    private fun RenderNewVersionDownloadItem(downloadProgress: Int, isOnDownload: Boolean, context: Context, version: SettingsViewModel.DownloadInf, viewModel: SettingsViewModel.VersionSettingViewModel){
        Settings().RenderSettingBoxItem(version.tag, content = {
            Text(buildAnnotatedString {
                append("新的版本:")
                withStyle(style = SpanStyle(color = Color.Blue)){append(version.tag)}
                append(",下载地址:\n")
                withLink(LinkAnnotation.Url(
                    url = version.url,
                    styles = TextLinkStyles(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline))
                )){append(version.url)}
                if (isOnDownload){
                    append("正在下载中...")
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = {(downloadProgress/100).toFloat()})
                }
            })
            Button(
                onClick = {viewModel.downloadUpdateApp()},
                enabled = !isOnDownload
            ){
                Text("下载版本")
            }

        })
    }
    @Composable
    private fun RenderNewVersionDownloadInstall(file: File,context: Context,version: SettingsViewModel.DownloadInf,viewModel: SettingsViewModel.VersionSettingViewModel){
        Settings().RenderSettingBoxItem(version.tag, content = {
            Text("版本:${version.tag}已安装完成,文件地址:${file.name}")
            Button(onClick = {}){Text("安装")}
        })
    }
    @Composable
     fun RenderDialog(onClose:()-> Unit,title: String,des: String,onClick:()-> Unit){
        AlertDialog(
            onDismissRequest = {onClose()},
            title ={Text(title)},
            text={Text(des)},
            confirmButton = { TextButton(onClick ={ onClick() }){Text("确认")} },
            dismissButton = {TextButton(onClick = {onClose()}){Text("取消")} }
        )
    }
}