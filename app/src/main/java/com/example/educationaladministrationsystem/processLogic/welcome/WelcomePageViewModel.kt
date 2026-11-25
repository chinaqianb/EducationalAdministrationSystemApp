package com.example.educationaladministrationsystem.processLogic.welcome

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.educationaladministrationsystem.processLogic.DataSave
import com.example.educationaladministrationsystem.processLogic.eASWeb.AEScrypt.PasswordCrypt.decryptPassword
import com.example.educationaladministrationsystem.processLogic.eASWeb.AEScrypt.PasswordCrypt.encryptPassword
import com.example.educationaladministrationsystem.processLogic.eASWeb.AEScrypt.decryptAES
import com.example.educationaladministrationsystem.processLogic.eASWeb.AEScrypt.encryptAES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.Base64

class WelcomePageViewModel(androidApplication: Application) : AndroidViewModel(androidApplication) {
    private val _currentPage = MutableStateFlow("0")
    val currentPage =_currentPage.asStateFlow()
    private val _data=MutableStateFlow<LoginData>(LoginData("","","",false))
    val data=_data.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading= _isLoading.asStateFlow()
    private val _isHashError=MutableStateFlow(false)
    val isHashError=_isHashError.asStateFlow()
    init {
        getSaveFromData()
    }
    fun setPage(page: String){
        _currentPage.value=page
    }
    data class LoginData(val username: String, val password: String,val url: String,val isJsOn: Boolean)
    private fun getSaveFromData(){
        viewModelScope.launch {
            _isLoading.value=true
            withContext(Dispatchers.IO) {
                val dataSave = DataSave(application.applicationContext)
                val save = dataSave.read(dataSave.LOGIN_SETTING_NAME)
                _data.value = LoginData(
                    save["user_name"].orEmpty().decryptAES(),
                    save["password"].orEmpty().decryptPassword(),
                    save["login_url"].orEmpty(),
                    save["js_inject"].orEmpty() == "1"
                )
            }
            _isLoading.value=false
        }
    }
    fun putData(loginData: LoginData){
        _data.value=loginData
    }
    private fun saveData(){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val dataSave = DataSave(application.applicationContext)
                val map = mapOf(
                    "user_name" to _data.value.username.encryptAES(),
                    "password" to _data.value.password.encryptPassword(),
                    "login_url" to _data.value.url,
                    "js_inject" to if (_data.value.isJsOn) "1" else "0"
                )
                dataSave.save(map, dataSave.LOGIN_SETTING_NAME)
            }
        }
    }
    private fun hashData(value: String): String{
        val salt="gtyf"
        val saltedPassword=salt+value
        val md= MessageDigest.getInstance("SHA-256")
        val byte=md.digest(saltedPassword.toByteArray())
        return Base64.getEncoder().encodeToString(byte)
    }
    private fun checkIfUrlRight(url:String): Boolean{
      return hashData(url)=="tvO547BcNQOMb+lLC1uX6g4e+C3K5udBSZ0pJIt2PKM="
    }
    fun checkAndSaveData(){
        if (!checkIfUrlRight(_data.value.url)){
           _isHashError.value=true
            return
        }
        _isHashError.value=false
        saveData()
        setPage("2")
    }

}