package com.example.educationaladministrationsystem.processLogic.settings

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PackageManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educationaladministrationsystem.processLogic.DataSave
import com.example.educationaladministrationsystem.processLogic.settings.versionUpload.Api
import com.example.educationaladministrationsystem.processLogic.settings.versionUpload.GitHubRelease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.security.DigestInputStream
import java.security.MessageDigest

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
                _data.value=dataSave.read()
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
                    dataSave.save(_data.value)
                    _changeSuccess.emit(Unit)
                } catch (e: Exception) {
                    _changeError.emit(e.message?:"未知")
                }
            }
        }
    }
    class VersionSettingViewModel: ViewModel(){
        private val _noNeedUpdate=MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val noNeedUpdate=_noNeedUpdate.asSharedFlow()
        private val _needUpdate=MutableSharedFlow<DownloadInf>(extraBufferCapacity = 1)
        val needUpdate=_needUpdate.asSharedFlow()
        private val _hasError=MutableSharedFlow<String>(extraBufferCapacity = 1)
        val hasError=_hasError.asSharedFlow()
        private val _apkReady=MutableSharedFlow<File>(extraBufferCapacity = 1)
        val apkReady=_apkReady.asSharedFlow()
        private var _toSettingPage=MutableStateFlow(false)
        val toSettingPage=_toSettingPage.asStateFlow()
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
        data class DownloadInf(val tag: String,val url: String,val fileHash: String)
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
                    val data=Api.service.getVersionUpload()
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
                        _needUpdate.emit(DownloadInf(lastestVersion,lastestVersionUrl?:"",fileHash?:""))
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
        fun downNewVersion(inf: DownloadInf,context: Context){
            viewModelScope.launch {
                if (inf.url.isEmpty() || inf.tag.isEmpty()) {
                    _hasError.emit("下载地址是空的")
                    return@launch
                }
                val downloadManager=context.getSystemService(Context.DOWNLOAD_SERVICE)as DownloadManager
                val downloadCompleteReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                        if (id != downloadId) return

                        // 注销，避免重复触发
                        context.unregisterReceiver(this)

                        viewModelScope.launch(Dispatchers.IO) {
                            handleDownloadComplete(context, downloadManager,downloadId, inf)
                        }
                    }
                }
                ContextCompat.registerReceiver(
                    context,
                    downloadCompleteReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
                val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(downloadCompleteReceiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    @Suppress("UnspecifiedRegisterReceiverFlag")
                    context.registerReceiver(downloadCompleteReceiver, filter)
                }

                val request = DownloadManager.Request(Uri.parse(inf.url))
                    .setTitle("正在下载版本:${inf.tag}")
                    .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS,"EAS-${inf.tag}.apk")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

                val downloadId =downloadManager.enqueue(request)
                val fileHash=inf.fileHash


            }
        }
        private var downloadId: Long = -1L
        private suspend fun handleDownloadComplete(
            context: Context,
            dm: DownloadManager,
            id: Long,
            inf: DownloadInf
        ) {
            // 1. 查询下载状态
            dm.query(DownloadManager.Query().setFilterById(id)).use { cursor ->
                if (cursor == null || !cursor.moveToFirst()) {
                    _hasError.emit("下载记录不存在")
                    return
                }
                val status = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                )
                if (status != DownloadManager.STATUS_SUCCESSFUL) {
                    _hasError.emit("下载未成功，状态码：$status")
                    return
                }
            }

            // 2. 获取本地文件 URI 和 File 对象
            val uri = dm.getUriForDownloadedFile(id)
                ?: run {
                    _hasError.emit("无法获取下载文件")
                    return
                }

            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "EAS-${inf.tag}.apk"
            )
            if (!file.exists()) {
                _hasError.emit("文件不存在")
                return
            }

            // 3. 流式计算 SHA-256
            val computedHash = computeSha256(uri, context)
            if (!computedHash.equals(inf.fileHash, ignoreCase = true)) {
                file.delete()
                _hasError.emit("SHA-256 校验失败\n本地：$computedHash\n预期：${inf.fileHash}")
                return
            }

            // 4. 校验通过，通知 UI（不在此处直接安装）
            _apkReady.emit(file)
        }
        private fun computeSha256(uri: Uri, context: Context): String {
            val digest = MessageDigest.getInstance("SHA-256")
            context.contentResolver.openInputStream(uri)?.use { input ->
                DigestInputStream(input, digest).use { dis ->
                    val buffer = ByteArray(8192)
                    while (dis.read(buffer) != -1) { /* 流式消费，自动更新 digest */ }
                }
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
        fun installAPK(context: Context,file:File){
            //权限检测
            if (!context.packageManager.canRequestPackageInstalls()){
                val intent= Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data= Uri.parse("package:${context.packageName}")
                }
                _toSettingPage.value=true
                context.startActivity(intent)
                return
            }
            if (!file.exists()){
                return
            }
            val apkUri= FileProvider.getUriForFile(context,"${context.packageName}.fileprovider",file)
            val installIntent= Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri,"application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        }
        fun setToSetting(state: Boolean){
            _toSettingPage.value=state
        }
    }
}