package com.example.educationaladministrationsystem.width.settings

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educationaladministrationsystem.processLogic.settings.SettingsViewModel
import com.mikepenz.markdown.m3.Markdown
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
            }
        }
        LaunchedEffect(Unit){
            viewModel.needUpdate.collect {
                dialogTitle="更新新版本"
                dialogMessage="- 发现新的版本${it.tag}\r\n- 下载地址${it.url}\r\n- 更新内容:\r\n${it.text}"
                onClickEvent={//viewModel.downNewVersion(SettingsViewModel.DownloadInf(it.tag,it.url,it.fileHash),context);
                    showDialog=false}
                showDialog=true
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
        Column(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF6F6F6))
            .verticalScroll(rememberScrollState()),
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
            TestMarkdown()
        }
    }
    @Composable
    private fun TestMarkdown(){
        Settings().RenderSettingBoxItem("测试markdown", content = {
            val content="- 修复了安装新版本之后，提醒失效的bug\r\n- 修复了webview显示错误的bug\r\n- 修改了EAS页面的架构，增加了启动的速度\r\n- 修复了进入webview界面重复重复同步课表的操作\r\n- 修复了由于教务系统的变动导致的课表无法正常同步的情况\r\n\r\n**Full Changelog**: https://github.com/chinaqianb/EducationalAdministrationSystemApp/compare/1.3.1...1.3.2"
            Text("内容的")
            Markdown(content)
            Text("无内容的")
            Markdown("未发现新版本")
        })
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
                append("\nrelease:")
                withLink(LinkAnnotation.Url(
                    url = version.htmlUrl,
                    styles = TextLinkStyles(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline))
                )){append(version.htmlUrl)}
                append("\n下载地址:")
                withLink(LinkAnnotation.Url(
                    url = version.url,
                    styles = TextLinkStyles(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline))
                )){append(version.url)}

                if (isOnDownload){
                    append("正在下载中...")
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = {(downloadProgress/100).toFloat()})
                }
            })
            Markdown(content = version.text)
//            Button(
//                onClick = {viewModel.downloadUpdateApp()},
//                enabled = !isOnDownload
//            ){
//                Text("下载版本")
//            }//下载功能禁用

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
            text={Markdown(des,modifier = Modifier
                .wrapContentHeight()
                .heightIn(max = 400.dp))},
            confirmButton = { TextButton(onClick ={ onClick() }){Text("确认")} },
            dismissButton = {TextButton(onClick = {onClose()}){Text("取消")} }
        )
    }
}