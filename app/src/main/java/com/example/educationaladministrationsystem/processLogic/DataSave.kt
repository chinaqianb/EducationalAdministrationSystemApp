package com.example.educationaladministrationsystem.processLogic

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.educationaladministrationsystem.processLogic.classTable.KbItem
import com.example.educationaladministrationsystem.processLogic.classTable.RqazcItem
import com.example.educationaladministrationsystem.width.classTable.ClassTable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.collections.emptyList

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name="educational_administration_system")
class DataSave(context: Context) {
    private val dataStore=context.dataStore
    private val json= Json { ignoreUnknownKeys=true }
    //key
    val LOGIN_SETTING_NAME= stringPreferencesKey("login_setting_name")
    val ABOUT_US_DATA=stringPreferencesKey("about_us_data")
    val DOWNLOAD_STATE=stringPreferencesKey("download_state")
    val EAS_COOKIES=stringPreferencesKey("eas_cookies")
    val CLASS_TABLE_SAVE=stringPreferencesKey("class_table_save")
    val CLASS_TABLE_SETTING=stringPreferencesKey("class_table_setting")
    val CLASS_TABLE_SAVE_WEEKDATE=stringPreferencesKey("class_table_asve_week_date")
    //save
    suspend fun save(data: Map<String, String>,key: Preferences.Key<String>){

        val data =json.encodeToString(MapSerializer(String.serializer(), String.serializer()),data)
        dataStore.edit { it[key]=data }
    }
    suspend fun saveClassData(data: Map<String, List<KbItem>>){
        val data =json.encodeToString(MapSerializer(String.serializer(), ListSerializer(KbItem.serializer())),data)
        dataStore.edit { it[CLASS_TABLE_SAVE]=data }
    }
    suspend fun saveClassWeekDate(data: Map<String, List<RqazcItem>>){
        val data =json.encodeToString(MapSerializer(String.serializer(), ListSerializer(RqazcItem.serializer())),data)
        dataStore.edit { it[CLASS_TABLE_SAVE_WEEKDATE]=data }
    }
    //read
    suspend fun read(key: Preferences.Key<String>): Map<String, String>{
        return dataStore.data.map{
            preferences ->
            val data=preferences[key]?:""
            if (data.isEmpty())emptyMap()
            else json.decodeFromString(MapSerializer(String.serializer(), String.serializer()),data)
        }.first()
    }
    suspend fun readClassData():Map<String, List<KbItem>>{
        return dataStore.data.map{
                preferences ->
            val data=preferences[CLASS_TABLE_SAVE]?:""
            if (data.isEmpty())emptyMap()
            else json.decodeFromString(MapSerializer(String.serializer(), ListSerializer(KbItem.serializer())),data)
        }.first()
    }
    suspend fun readClassWeekDate():Map<String, List<RqazcItem>>{
        return dataStore.data.map{
                preferences ->
            val data=preferences[CLASS_TABLE_SAVE_WEEKDATE]?:""
            if (data.isEmpty())emptyMap()
            else json.decodeFromString(MapSerializer(String.serializer(), ListSerializer(RqazcItem.serializer())),data)
        }.first()
    }

}