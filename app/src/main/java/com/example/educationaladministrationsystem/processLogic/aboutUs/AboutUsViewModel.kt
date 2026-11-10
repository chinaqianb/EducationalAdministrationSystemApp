package com.example.educationaladministrationsystem.processLogic.aboutUs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educationaladministrationsystem.processLogic.DataSave
import com.example.educationaladministrationsystem.processLogic.settings.versionUpload.Api
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AboutUsViewModel(application: Application): AndroidViewModel(application) {
    private val _data = MutableStateFlow<Map<String, String>>(emptyMap())
    private val _isGet = MutableStateFlow<AuthorInfo>(AuthorInfo("","",""))
    val isGet=_isGet.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading=_isLoading.asStateFlow()
    private val save = DataSave(application.applicationContext)
    data class AuthorInfo(val name: String,val authorImage: String,val url: String)
    init {
        viewModelScope.launch {
            _isLoading.value=true
            _data.value = save.read(save.ABOUT_US_DATA)
            _isGet.value= AuthorInfo(_data.value.getOrElse("name",{""}),
                _data.value.getOrElse("author_image",{""}),
                _data.value.getOrElse("url",{""}))
            if (_isGet.value.name.isEmpty()||_isGet.value.authorImage.isEmpty()||_isGet.value.url.isEmpty()){
                getAuthor()
            }
            _isLoading.value=false
        }
    }

    suspend fun getAuthor(){
            try {
                _isLoading.value=true
                val data =Api.service.getAuthor()
                val info = AuthorInfo(data.name,data.avatarUrl,data.htmlUrl)
                _isLoading.value=false
                _isGet.value=info
            } catch (e: Exception) {
                throw e
            }
    }
}