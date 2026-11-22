package com.example.educationaladministrationsystem.processLogic.settings

import android.app.AlarmManager
import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.chiclaim.android.downloader.DOWNLOAD_ENGINE_EMBED
import com.chiclaim.android.downloader.DownloadListener
import com.chiclaim.android.downloader.DownloadRequest
import com.example.educationaladministrationsystem.R
import com.example.educationaladministrationsystem.processLogic.DataSave
import com.example.educationaladministrationsystem.processLogic.EASNotice
import com.example.educationaladministrationsystem.processLogic.classTable.ClassTableApi
import com.example.educationaladministrationsystem.processLogic.classTable.ClassTableClock
import com.example.educationaladministrationsystem.processLogic.classTable.ClassTableReceiver
import com.example.educationaladministrationsystem.processLogic.classTable.ClassTableViewModel
import com.example.educationaladministrationsystem.processLogic.classTable.ClassTableViewModel.GetClassTableListData
import com.example.educationaladministrationsystem.processLogic.classTable.KbItem
import com.example.educationaladministrationsystem.processLogic.classTable.RqazcItem
import com.example.educationaladministrationsystem.processLogic.settings.versionUpload.VersionApi
import com.example.educationaladministrationsystem.processLogic.settings.versionUpload.GitHubRelease
import com.fleeksoft.ksoup.Ksoup
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import com.composables.icons.lucide.R.drawable as Lucide

class SettingsViewModel(application: Application): AndroidViewModel(application) {

    class LoginSettingViewModel(application: Application): AndroidViewModel(application){
        private val _data = MutableStateFlow<Map<String, String>>(emptyMap())
        val data=_data.asStateFlow()
        private val dataSave= DataSave(application.applicationContext)
        private val _userName=MutableStateFlow("")
        val userName=_userName.asStateFlow()
        private val _password=MutableStateFlow("")
        val password=_password.asStateFlow()
        private val _loginUrl=MutableStateFlow("")
        val loginUrl=_loginUrl.asStateFlow()
        private val _changeSuccess= MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val changeSuccess= _changeSuccess.asSharedFlow()
        private val _changeError=MutableSharedFlow<String>(extraBufferCapacity = 1)
        val changeError= _changeError.asSharedFlow()
        init {
            viewModelScope.launch {
                _data.value=dataSave.read(dataSave.LOGIN_SETTING_NAME)
                _userName.value=_data.value.getOrElse("user_name", { "" })
                _password.value=_data.value.getOrElse("password",{""})
                _loginUrl.value=_data.value.getOrElse("login_url",{""})
            }
        }
        fun someDataChange(user: String=_userName.value,password: String=_password.value,url: String=_loginUrl.value){
            _userName.value=user
            _password.value=password
            _loginUrl.value=url
        }
        fun changeData(){
            viewModelScope.launch {
                try {
                    val data: Map<String, String> =mapOf("user_name" to _userName.value,"password" to _password.value,"login_url" to _loginUrl.value)
                    _data.value=data
                    dataSave.save(_data.value, dataSave.LOGIN_SETTING_NAME)
                    _changeSuccess.emit(Unit)
                } catch (e: Exception) {
                    _changeError.emit(e.message?:"未知")
                }
            }
        }
    }
    data class DownloadInf(val tag: String,val url: String,val fileHash: String)
    class VersionSettingViewModel(application: Application): AndroidViewModel(application), DownloadListener {
        private val _noNeedUpdate=MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val noNeedUpdate=_noNeedUpdate.asSharedFlow()
        private val _needUpdate=MutableSharedFlow<DownloadInf>(extraBufferCapacity = 1)
        val needUpdate=_needUpdate.asSharedFlow()
        private val _newVersionInf =MutableStateFlow<DownloadInf>(DownloadInf("","",""))
        val newVersionInf=_newVersionInf.asStateFlow()
        private val _hasNewVersion =MutableStateFlow(false)
        val hasNewVersion=_hasNewVersion.asStateFlow()
        private val _hasReadyApk=MutableStateFlow(false)
        val hasReadyApk=_hasReadyApk.asStateFlow()
        private val _isOnDownload =MutableStateFlow(false)
        val isOnDownload=_isOnDownload.asStateFlow()
        private val _hasError=MutableSharedFlow<String>(extraBufferCapacity = 1)
        val hasError=_hasError.asSharedFlow()
        private val _apkReady=MutableSharedFlow<File>(extraBufferCapacity = 1)
        val apkReady=_apkReady.asSharedFlow()
        private var _toSettingPage=MutableStateFlow(false)
        val toSettingPage=_toSettingPage.asStateFlow()
        private var _downloadProgress =MutableStateFlow(0)
        val downloadProgress =_downloadProgress.asStateFlow()
        private val appContext = application.applicationContext
        private val dataSave = DataSave(appContext)
        // 不受 ViewModel 生命周期影响，卸载/重建也能跑完



        init {
            loadLocalNewVersion()


        }
        private fun loadLocalNewVersion(){
            viewModelScope.launch {
                val data : Map<String, String> =dataSave.read(dataSave.DOWNLOAD_STATE)
                _newVersionInf.value= DownloadInf(data.getOrDefault("tag",""),
                    data.getOrDefault("url",""),
                    data.getOrDefault("hash","")
                    )
                if (_newVersionInf.value.tag.isNotEmpty()){
                    _hasNewVersion.value=true
                }
            }
        }



        fun getVersion(context: Context): String{
            return try {
                val pm =context.packageManager
                val pkgInf=if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
                    pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
                }else{
                    @Suppress
                    pm.getPackageInfo(context.packageName,0)
                }
                pkgInf.versionName?:""
            }catch (e: PackageManager.NameNotFoundException){
                ""
            }
        }

        /**
         * @version 传入的版本号
         */
        fun getThreeLay(version: String): Triple<Int, Int, Int> {
            val parts = version
                .trim()
                .removePrefix("v")
                .removePrefix("V")
                .substringBefore("-")
                .split('.')                     // ← 关键：按字符 '.' 分割
                .map { it.trim().toIntOrNull() ?: 0 }

            return Triple(
                parts.getOrElse(0) { 0 },
                parts.getOrElse(1) { 0 },
                parts.getOrElse(2) { 0 }
            )
        }

        fun checkVersionUpdate(context: Context){
            viewModelScope.launch {
                Log.d("check","协程已启动")
                try {
                    val myVersion=getVersion(context)
                    val data=VersionApi.service.getVersionUpload()
                    val lastestVersion=data.tagName
                    Log.d("check","我的版本:${myVersion},最新:${lastestVersion}")
                    if (lastestVersion.isEmpty()){Log.d("check","no_need_empty");_noNeedUpdate.emit(Unit);return@launch}
                    val myThr=getThreeLay(myVersion)
                    val lastestThr=getThreeLay(lastestVersion)
                    Log.d("check","myVersionT:${myThr}")
                    Log.d("check","lastestT:${lastestThr}")
                    if (lastestThr.first==myThr.first&&lastestThr.second==myThr.second&&lastestThr.third==myThr.third){
                        Log.d("check","no_need_equals")
                        _noNeedUpdate.emit(Unit)
                        return@launch
                    }
                    if (lastestThr.first>myThr.first
                        ||(lastestThr.first==myThr.first&&lastestThr.second>myThr.second)
                        ||(lastestThr.first==myThr.first&&lastestThr.second==myThr.second&&lastestThr.third>myThr.third)){
                        val lastestVersionUrl=getDownloadUrl(data)
                        val fileHash=getDownloadFileHash(data)
                        Log.d("check","need")
                        val inf=DownloadInf(lastestVersion,lastestVersionUrl?:"",fileHash?:"")
                        _newVersionInf.value= inf
                        _hasNewVersion.value=true
                        dataSave.save(
                            mapOf( "tag" to lastestVersion,
                                "hash" to inf.fileHash, "url" to inf.url),
                            dataSave.DOWNLOAD_STATE
                        )
                        _needUpdate.emit(inf)
                    }
                } catch (e: Exception) {
                    Log.d("check","error:${e.message}")
                   _hasError.emit(e.message?:"")
                }

            }

        }
        private fun getDownloadUrl(data: GitHubRelease): String? {
            return data.assets.firstOrNull{ it.name.endsWith(".apk") }?.browserDownloadUrl
        }
        private fun getDownloadFileHash(data: GitHubRelease): String? {
            return data.assets.firstOrNull{ it.name.endsWith(".apk") }?.digest
        }
        fun downloadUpdateApp(){
            val request = DownloadRequest(application.applicationContext,_newVersionInf.value.url, DOWNLOAD_ENGINE_EMBED)
                .setNotificationTitle("版本更新-${_newVersionInf.value.tag}")
                .setNotificationContent("下载版本-${_newVersionInf.value.tag}")
                .setNeedInstall(true)
                .setNotificationSmallIcon(Lucide.lucide_ic_arrow_down_to_line)
                .setShowNotificationDisableTip(true)
                .registerListener(this)
                .startDownload()

        }





        /*
         val expected = inf.fileHash.substringAfter("sha256:").trim()
            val computed = computeSha256(uri)       // 见下，处理 null stream
            if (!computed.equals(expected, ignoreCase = true)) {
                file.delete()
                _hasError.emit("SHA-256 校验失败\n本地：$computed\n预期：$expected")
                Log.d("check", "sha-256 fail 本地:$computed 预期:$expected")
                return
            }
         */
        private fun computeSha256(uri: Uri): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val input = appContext.contentResolver.openInputStream(uri) ?: return ""
            input.use { ins ->
                DigestInputStream(ins, digest).use { dis ->
                    val buf = ByteArray(8192)
                    while (dis.read(buf) != -1) { }
                }
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }


        fun setToSetting(state: Boolean) { _toSettingPage.value = state }


        fun handEmit(){
            viewModelScope.launch {
                val file =
                    File("/storage/emulated/0/Android/data/com.example.educationaladministrationsystem/files/Download/EAS-1.1.0.apk")
                _apkReady.emit(file)
            }
        }

        override fun onDownloadComplete(uri: Uri) {
           _isOnDownload.value=false

        }

        override fun onDownloadFailed(e: Throwable) {
            _isOnDownload.value=false
            Log.d("check","下载失败by: ${e.message}")
        }

        override fun onDownloadStart() {
            _isOnDownload.value=true
        }

        override fun onProgressUpdate(percent: Int) {
            _downloadProgress.value=percent
        }
    }
    class ClassTableSettingViewModel(application: Application): AndroidViewModel(application){
        data class DialogMes(val title: String,val mes: String)
        private val _sureCancel =MutableSharedFlow<DialogMes>()
        val sureCancel=_sureCancel.asSharedFlow()
        private val _sureOpen =MutableSharedFlow<DialogMes>()
        val sureOpen=_sureOpen.asSharedFlow()
        private val _errorOpen =MutableSharedFlow<DialogMes>()
        val errorOpen = _errorOpen.asSharedFlow()
        private val _isOnNotice=MutableStateFlow(false)
        val isOnNotice =_isOnNotice.asStateFlow()
        private val _isOnClock =MutableStateFlow(false)
        val isOnClock =_isOnClock.asStateFlow()
        private val _hasClock=MutableStateFlow(false)
        val hasClock =_hasClock.asStateFlow()
        private val _getListNum=MutableStateFlow("1")
        val getListNum=_getListNum.asStateFlow()
        private val _loading =MutableStateFlow(false)
        val loading =_loading.asStateFlow()
        private val _isOnSync=MutableStateFlow(false)
        val isOnSync =_isOnSync.asStateFlow()
        private val _getListNumUnlegal =MutableStateFlow(false)
        val getListNumUnlegal =_getListNumUnlegal.asStateFlow()
        init {
            viewModelScope.launch {
                _loading.value=true
                val dataSave = DataSave(application.applicationContext)
                val data =dataSave.read(dataSave.CLASS_TABLE_SETTING)
                _isOnNotice.value = data["is_on_notice"]=="1"
                _isOnClock.value = data["is_on_clock"]=="1"
                _getListNum.value= data["get_list_num"]?:"1"
                _hasClock.value = isClockExist(application.applicationContext)
                _loading.value=false
            }
        }
        fun setIsOnNotice(value:Boolean){
            _isOnNotice.value=value
        }
        fun setIsOnClock(value: Boolean){
            _isOnClock.value=value
        }
        fun setHasClock(){
            _hasClock.value=isClockExist(application.applicationContext)
        }
        fun setGetListNum(value: String){
            _getListNum.value=value
        }
        fun saveSettings(){
            viewModelScope.launch {
                if (_getListNum.value.isEmpty()||_getListNum.value.toIntOrNull()==null||_getListNum.value.toInt()<=0){
                    _getListNumUnlegal.value=true
                    return@launch
                }
                _getListNumUnlegal.value=false
                val dataSave = DataSave(application.applicationContext)
                dataSave.save(
                    mapOf("is_on_notice" to if (_isOnNotice.value) "1" else "0",
                        "is_on_clock" to if (_isOnClock.value) "1" else "0",
                    "get_list_num" to _getListNum.value),
                    dataSave.CLASS_TABLE_SETTING
                )
            }
        }
        fun checkNoticePermission(): Boolean{
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
                if (ContextCompat.checkSelfPermission(application.applicationContext, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ){
                   return false
                }
            }

            return true
        }
        fun checkHasPermission(permission: ManagedActivityResultLauncher<String, Boolean>,canNotice:()-> Unit): Boolean{
            var can =false
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
                if (ContextCompat.checkSelfPermission(application.applicationContext, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ){
                    //申请权限
                    permission.launch(android.Manifest.permission.POST_NOTIFICATIONS)

                }else{
                    can=true
                    canNotice()
                }
            }else{
                can=true
            }
            return can
        }
        fun checkHasClockPermission(context: Context): Boolean{
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                if (!alarmManager.canScheduleExactAlarms()) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return false
                }

            }
            return true
        }
        private fun getClock(context: Context): PendingIntent?{
            val intent = Intent(context, ClassTableReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                5,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            return pendingIntent
        }
        fun isClockExist(context: Context): Boolean{
            return getClock(context) != null
        }
        fun needCancelClock(){
            viewModelScope.launch {
                _sureCancel.emit(DialogMes("取消提醒","确定取消提醒吗?"))
            }
        }
        fun cancelClock(context: Context){
            val clock=getClock(context)
            if (clock!=null){
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(clock)
                clock.cancel()
                _hasClock.value=false
            }
        }
        fun needOpenClock(context: Context,noticePermission: ManagedActivityResultLauncher<String, Boolean>){
            viewModelScope.launch {
                //检查权限和缓存中是否有内容
                if (!checkHasPermission(noticePermission){}){
                    _errorOpen.emit(DialogMes("没有通知权限","应用没有系统通知的权限，请到设置中打开"))
                    return@launch
                }
                if (!checkHasClockPermission(context)){
                    _errorOpen.emit(DialogMes("没有设置提醒的权限","应用没有设置提醒的权限，请到设置中打开"))
                    return@launch
                }
                val dataSave= DataSave(context)
                val week=dataSave.readClassData()
                if (week.isEmpty()){
                    _errorOpen.emit(DialogMes("本地数据中课表未找到","在本地数据中未找到存储的课表，请先同步课表"))
                    return@launch
                }
                _sureOpen.emit(DialogMes("是否开启提醒","相关权限及其本地课表均已找到,是否开启"))
            }
        }
        fun openClock(context: Context){
            viewModelScope.launch {
                ClassTableClock.handleWeekDate(context)
                setHasClock()
            }
        }

        fun testNotice(permission: ManagedActivityResultLauncher<String, Boolean>){
          checkHasPermission(permission,{
              NotificationManagerCompat.from(application.applicationContext)
                  .notify(1201,createNotice("测试标题","文字"))

          })

        }
        private fun createNotice(title: String,text: String):Notification {
            val notice= NotificationCompat.Builder(application.applicationContext, EASNotice.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notice)
                .setColor(0x287FB9)
                .setContentTitle(title)
                .setContentText(text)
                .setOngoing(true)//常住
                .setPriority(NotificationCompat.PRIORITY_LOW)
//                .setTimeoutAfter(TimeUnit.MINUTES.toMillis(10))//10分钟后删除
                .setTimeoutAfter(10000)
                .build()
            //发送
            //NotificationManagerCompat.from(application.applicationContext).notify(1201,notice)//每条通知的唯一id
            return notice
        }
        private fun hasClock(context: Context){//好像是在主线程中写的
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.S){
                val alarmManager=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()){
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(intent)
                    return
                }
                val trigger= SystemClock.elapsedRealtime()+1000*60*5//5分钟

                val myIntent= Intent(context,ClassTableReceiver::class.java)
                val pending= PendingIntent.getBroadcast(context,0,myIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    trigger,
                    pending)
            }
        }
        fun syncClassTable(){
            viewModelScope.launch {
                _isOnSync.value=true
                ClassTableClock.getClassTableList(application.applicationContext)
                _isOnSync.value=false
            }
        }

    }
}