package com.example.educationaladministrationsystem.processLogic.settings

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.PackageManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educationaladministrationsystem.processLogic.DataSave
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
        fun getThreeLay(version:String): Triple<String, String, String>{
            val parts =version.split("\\.")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val padded =parts +List(3-parts.size.coerceAtLeast(3)){"0"}
            return Triple(padded[0],padded[1],padded[2])
        }
    }
}