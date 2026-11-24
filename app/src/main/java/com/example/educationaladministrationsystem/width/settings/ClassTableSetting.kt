package com.example.educationaladministrationsystem.width.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educationaladministrationsystem.processLogic.settings.SettingsViewModel
import java.util.Locale
import com.composables.icons.lucide.R.drawable as Lucide

class ClassTableSetting {

    @Composable
    fun InitClassTableSetting(paddingValues: PaddingValues,viewModel: SettingsViewModel.ClassTableSettingViewModel= viewModel()){
        val activityResultLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())
            {isGranted-> viewModel.noticeRequestPermissionCallBack(isGranted) }
        val isOnNotice by viewModel.isOnNotice.collectAsState()
        val isOnClock by viewModel.isOnClock.collectAsState()
        var onClickEvent by remember { mutableStateOf<(() -> Unit)?>(null)}
        var showDialog by remember { mutableStateOf(false) }
        var dialogTitle by remember { mutableStateOf("") }
        var dialogMessage by remember { mutableStateOf("") }
        val cancel by viewModel.hasClock.collectAsState()
        val loading by viewModel.loading.collectAsState()
        var onOpenClockDialog by remember { mutableStateOf(false) }
        val context=LocalContext.current
        LaunchedEffect(Unit){
            viewModel.sureOpen.collect {e->
                onOpenClockDialog=true
            }
        }
        LaunchedEffect(Unit){
            viewModel.sureCancel.collect {e->
                dialogTitle=e.title
                dialogMessage=e.mes
                onClickEvent={viewModel.cancelClock(context);showDialog=false}
                showDialog=true
            }
        }
        LaunchedEffect(Unit){
            viewModel.errorOpen.collect {
                dialogTitle=it.title
                dialogMessage=it.mes
                onClickEvent={showDialog=false}
                showDialog=true
            }
        }
        LifecycleResumeEffect(Unit){
            //切回前台回调

            onPauseOrDispose {  }
        }
        if (showDialog&&dialogTitle.isNotEmpty()){
            VersionInfoSetting().RenderDialog({showDialog=false},dialogTitle,dialogMessage,{onClickEvent?.invoke()})
        }
        if (onOpenClockDialog){
            AskOpenDialog(context, viewModel,{onOpenClockDialog=false},{onOpenClockDialog=false})
        }
        if (loading){
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(30.dp))//转圈动画
            }
        }else {
            Column(
                Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF6F6F6)),
                verticalArrangement = Arrangement.Top
            ) {
                SyncClassTable(context, viewModel)
                RenderSetting(context, viewModel, activityResultLauncher, isOnNotice, isOnClock)
                if (isOnNotice && isOnClock) {
                    ClockSetting(
                        context,
                        viewModel,
                        { viewModel.needOpenClock(context, activityResultLauncher) },
                        cancel
                    )
                }
            }
        }
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SyncClassTable(context: Context,viewModel: SettingsViewModel.ClassTableSettingViewModel){
        val textNum by  viewModel.getListNum.collectAsState()
        val infiniteTransition = rememberInfiniteTransition(label = "refresh")
        val isOnSync by viewModel.isOnSync.collectAsStateWithLifecycle()
        var isMenuOpen by remember{ mutableStateOf(false) }
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "refreshAngle"
        )
        Settings().RenderSettingBoxItem("课表设置", content = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically){
                Text("进行课表同步")
                IconButton(onClick = {viewModel.syncClassTable()}){
                    Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_refresh_ccw), contentDescription = "refresh",
                        modifier = Modifier.rotate(if (isOnSync)angle else 0f))
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically){
                Text("课表获取个数")
                ExposedDropdownMenuBox(
                    expanded = isMenuOpen,
                    onExpandedChange = {isMenuOpen=it}
                ){
                    OutlinedTextField(
                        value = textNum,
                        onValueChange = {},
                        readOnly = true,                               // 只读，不弹键盘
                        label = { Text("获取个数") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMenuOpen)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)  // 关键：锚定

                    )
                    ExposedDropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = {isMenuOpen=false}
                    ){
                        viewModel.numList.forEach { e->
                            DropdownMenuItem(
                                text = {Text(e)},
                                onClick = {
                                    viewModel.setGetListNum(e)
                                    isMenuOpen=false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }
        })
    }
    @Composable
    private fun RenderSetting(context: Context,
        viewModel: SettingsViewModel.ClassTableSettingViewModel,
                              permission: ManagedActivityResultLauncher<String, Boolean>,
                              isOnNotice: Boolean,
                              isOnClock: Boolean

                             ){

        Settings().RenderSettingBoxItem("权限设置", content = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically) {
                Text("是否通知")
                Switch(checked = isOnNotice, onCheckedChange = {
                    value ->
                    if (!value)viewModel.setIsOnClock(false)
                    viewModel.setIsOnNotice(value && viewModel.checkAndOpenPermission(permission){})

                })

            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically){
                Text("启用提醒")
                Switch(checked = isOnClock, onCheckedChange = {
                    value->
                    viewModel.setIsOnClock( value && isOnNotice &&viewModel.checkHasClockAndOpenPermission(context))
                })
            }


        })

    }
    @Composable
    private fun ClockSetting(context: Context,viewModel: SettingsViewModel.ClassTableSettingViewModel,onOpen:()-> Unit,cancel: Boolean){
        Settings().RenderSettingBoxItem("提醒设置", content = {
            if (viewModel.isClockExist(context)){

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically){
                    Text("提醒下一节课")
                    Switch(checked = cancel, onCheckedChange = {value->
                        if (!value)viewModel.needCancelClock()
                    })
                }
            }else{
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,verticalAlignment = Alignment.CenterVertically){
                    Text("目前不存在提醒")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                    Button(onClick = {onOpen()}){
                        Text("创建提醒")
                    }
                }
            }
        })
    }


    @Composable
    fun AskOpenDialog(context: Context,viewModel: SettingsViewModel.ClassTableSettingViewModel,onClose:()-> Unit,onClick:()-> Unit){
        AlertDialog(
            onDismissRequest = {onClose()},
            title ={Text("是否开启提醒")},
            text={Text(buildAnnotatedString {
                append("通知权限,提醒权限均已开启。\n")
                append("由于国产安卓厂商限制,需开启")
                withStyle(style = SpanStyle(color = Color(0xFF6750A4))){append("自启动")}
                append("权限,否则无法提醒\n")
                append("如果没有开启，请前往设置开启自启动权限,如果已经开启或非国内手机可忽略\n")
                withLink(LinkAnnotation.Clickable(
                    tag = "open_start_muself",
                    styles = TextLinkStyles(SpanStyle(
                        textDecoration = TextDecoration.Underline, color = Color(0xFF6750A4)
                    )),
                    linkInteractionListener = {navigateToAutoStartSettings(context)}

                )){append("开启自启动权限->")}

            })},
            confirmButton = { TextButton(onClick ={ viewModel.openClock(context);onClick();viewModel.setHasClock() }){Text("确认")} },
            dismissButton = {TextButton(onClick = {onClose()}){Text("取消")} }
        )
    }
    fun navigateToAutoStartSettings(context: Context) {
        //获取自启动权限
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        val intent = Intent()
        try {
            when (manufacturer) {
                "xiaomi" -> intent.setComponent(
                    ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                )

                "huawei" -> intent.setComponent(
                    ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                    )
                )

                "oppo" -> intent.setComponent(
                    ComponentName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                    )
                )

                "vivo" -> intent.setComponent(
                    ComponentName(
                        "com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                    )
                )

                else -> {
                    intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.setData(Uri.fromParts("package", context.getPackageName(), null))
                }
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // 若上述页面不存在，则兜底跳转到应用详情页
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.setData(Uri.fromParts("package", context.getPackageName(), null))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}