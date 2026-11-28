package com.example.educationaladministrationsystem.processLogic.aboutUs

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.educationaladministrationsystem.processLogic.DataSave
import com.example.educationaladministrationsystem.processLogic.settings.versionUpload.VersionApi
import kotlinx.coroutines.flow.MutableStateFlow
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
                _data.value=mapOf("name" to _isGet.value.name,"author_image" to _isGet.value.authorImage,"url" to _isGet.value.url)
                save.save(_data.value,save.ABOUT_US_DATA)
            }
            _isLoading.value=false
        }
    }

    suspend fun getAuthor(){
            try {
                _isLoading.value=true
                val data =VersionApi.service.getAuthor()
                val info = AuthorInfo(data.name,data.avatarUrl,data.htmlUrl)
                _isLoading.value=false
                _isGet.value=info
            } catch (e: Exception) {
                throw e
            }
    }
    fun addGroup(context: Context) {
        joinQQGroup(context,"oPsv--mbqCzAQgXT7V8x5ycXX40pQo7S")
    }

    /****************
     *
     * 发起添加群流程。群号：教务系统 app 交流反馈(1106570018) 的 key 为： oPsv--mbqCzAQgXT7V8x5ycXX40pQo7S
     * 调用 joinQQGroup(oPsv--mbqCzAQgXT7V8x5ycXX40pQo7S) 即可发起手Q客户端申请加群 教务系统 app 交流反馈(1106570018)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     */
    fun joinQQGroup(context: Context, key: String?): Boolean {
        val intent = Intent()
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D" + key))
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            Toast.makeText(context, "你还没有安装QQ或该版本不支持", Toast.LENGTH_SHORT).show()
            return false
        }
    }
}