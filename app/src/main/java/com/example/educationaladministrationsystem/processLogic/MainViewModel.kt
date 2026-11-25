package com.example.educationaladministrationsystem.processLogic

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.educationaladministrationsystem.processLogic.DataSave
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _isFirst = MutableStateFlow(true)
    val isFirst=_isFirst.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading=_isLoading.asStateFlow()
    init {
        readIsFirst()
    }
    private fun readIsFirst(){
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                val dataSave = DataSave(application.applicationContext)
                val value = dataSave.read(dataSave.LOGIN_SETTING_NAME)["first_time"]
                _isFirst.value = (value ?: "") != "no"
            }
            _isLoading.value = false
        }
    }
    fun finishAllAndNavigate(){
        viewModelScope.launch {
            _isLoading.value=true
            withContext(Dispatchers.IO) {
                val dataSave = DataSave(application.applicationContext)
                val old = dataSave.read(dataSave.LOGIN_SETTING_NAME)
                dataSave.save(old + mapOf("first_time" to "no"), dataSave.LOGIN_SETTING_NAME)
            }
            _isFirst.value=false
            _isLoading.value=false

        }
    }
}