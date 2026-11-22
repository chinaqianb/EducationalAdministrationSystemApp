package com.example.educationaladministrationsystem.processLogic.classTable

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.educationaladministrationsystem.processLogic.DataSave
import com.fleeksoft.ksoup.Ksoup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.emptyMap

class ClassTableViewModel(application: Application) : AndroidViewModel(application) {
   private val _data = MutableStateFlow<Map<String, List<KbItem>>>(emptyMap())
    val data=_data.asStateFlow()
   private val _nowWeek =MutableStateFlow("")
    val nowWeek=_nowWeek.asStateFlow()
    private val _allWeek=MutableStateFlow("")
    val allWeek=_allWeek.asStateFlow()
    private val _index =MutableStateFlow(0)
    val index=_index.asStateFlow()
    private val _loading =MutableStateFlow(false)
    val loading =_loading.asStateFlow()
    data class GetClassTableListData(val xnm: String,val xqm: String,var nowWeek: String,val weekNum: String)
    init {
        tryGetList()
    }

    private suspend fun getClassTableList(){
        ClassTableClock.getClassTableList(application.applicationContext,{firstData, backData ->   _data.value=backData
            _allWeek.value=firstData.weekNum})
    }


    fun tryGetList(){
        viewModelScope.launch {
            _loading.value=true
            _data.value=getListFromDataSave()
            if (_data.value==emptyMap<String, List<KbItem>>()||_nowWeek.value==""||_allWeek.value==""){
                getClassTableList()
            }
            _loading.value=false

        }
    }
    fun indexGo(){
        _index.value++
    }
    fun indexBack(){
        _index.value--
    }
    suspend fun getListFromDataSave(): Map<String, List<KbItem>>{
        val dataSave=DataSave(application.applicationContext)
        val list=dataSave.readClassData()
        val some=dataSave.read(dataSave.CLASS_TABLE_SETTING)
        _nowWeek.value=some.getOrDefault("now_week","")
        if (_nowWeek.value.isNotEmpty()){
            _index.value=_nowWeek.value.toInt()
        }
        _allWeek.value=some.getOrDefault("week_num","")
        return list
    }
}