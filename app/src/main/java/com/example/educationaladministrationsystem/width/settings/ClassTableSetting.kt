package com.example.educationaladministrationsystem.width.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educationaladministrationsystem.processLogic.classTable.ClassTableClock
import com.example.educationaladministrationsystem.processLogic.settings.SettingsViewModel
import java.util.*
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
        val error by viewModel.error.collectAsState()
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
                SyncClassTable(context, viewModel,error)
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
    private fun SyncClassTable(context: Context,viewModel: SettingsViewModel.ClassTableSettingViewModel,error: ClassTableClock.ErrorInfo){
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
                Column {
                    Text("进行课表同步")
                    Text("将教务系统上的课表同步到本地", fontSize = 12.sp, color = Color.Gray)
                    if (error.isError)Text("同步错误by:${error.info},请确认登录状态,再次尝试", fontSize = 12.sp, color = Color.Red)
                }
                IconButton(onClick = {viewModel.syncClassTable()}){
                    Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_refresh_ccw), contentDescription = "refresh",
                        modifier = Modifier.rotate(if (isOnSync)angle else 0f))
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically){
                Column {
                    Text("课表获取个数")
                    Text("获取基于当前周的课表相邻的个数\n如当前为11周,获取个数为1,\n则本地课表有10,11,12周", fontSize = 12.sp, color = Color.Gray)
                }
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
                Column {
                    Text("是否通知")
                    Text("打开通知,用于提醒功能",fontSize = 12.sp, color = Color.Gray)
                }
                Switch(checked = isOnNotice, onCheckedChange = {
                    value ->
                    if (!value)viewModel.setIsOnClock(false)
                    viewModel.setIsOnNotice(value && viewModel.checkAndOpenPermission(permission){})

                })

            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically){
                Column {
                    Text("启用提醒")
                    Text("开启课程提醒\n软件将会在课程开始前的一段时间发送通知\n需要系统通知,提醒,和自启动权限" +
                            "\n目前版本为提前25分钟提醒\n在发送通知25分钟后自动消失", fontSize = 12.sp, color = Color.Gray)
                }
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