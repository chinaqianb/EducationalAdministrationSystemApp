package com.example.educationaladministrationsystem.processLogic.aboutUs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educationaladministrationsystem.processLogic.settings.versionUpload.Api
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AboutUsViewModel: ViewModel() {
    private val _isGet = MutableStateFlow<AuthorInfo>(AuthorInfo("","",""))
    val isGet=_isGet.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading=_isLoading.asStateFlow()
    data class AuthorInfo(val name: String,val authorImage: String,val url: String)
    fun getAuthor(){
        viewModelScope.launch {
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
}