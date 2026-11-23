package com.example.educationaladministrationsystem.processLogic

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.educationaladministrationsystem.processLogic.classTable.KbItem
import com.example.educationaladministrationsystem.processLogic.classTable.RqazcItem
import com.example.educationaladministrationsystem.width.classTable.ClassTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.collections.emptyList

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name="educational_administration_system")
class DataSave(context: Context) {
    private val appContext = context.applicationContext
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
    val MY_PAGE_SETTING=stringPreferencesKey("my_page_setting")
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

    suspend fun saveFileFromUri(uri: Uri, relativePath: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val target = File(appContext.filesDir, relativePath)
                target.parentFile?.mkdirs()
                appContext.contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                } ?: return@withContext null
                relativePath
            } catch (e: Exception) {
                null
            }
        }

    suspend fun deleteFile(relativePath: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val f = File(appContext.filesDir, relativePath)
                f.exists() && f.delete()
            } catch (e: Exception) {
                false
            }
        }

    suspend fun clearDir(relativeDir: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                File(appContext.filesDir, relativeDir).listFiles()?.forEach { it.delete() }
                true
            } catch (e: Exception) {
                false
            }
        }

    fun fileOf(relativePath: String): File = File(appContext.filesDir, relativePath)
}