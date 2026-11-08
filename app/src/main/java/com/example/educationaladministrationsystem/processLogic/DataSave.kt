package com.example.educationaladministrationsystem.processLogic

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.collections.emptyList

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name="educational_administration_system")
class DataSave(context: Context) {
    private val dataStore=context.dataStore
    private val json= Json { ignoreUnknownKeys=true }
    //key
    private val LOGIN_SETTING_NAME= stringPreferencesKey("login_setting_name")
    //save
    suspend fun save(data: Map<String, String>){

        val data =json.encodeToString(MapSerializer(String.serializer(), String.serializer()),data)
        dataStore.edit { it[LOGIN_SETTING_NAME]=data }
    }
    //read
    suspend fun read(): Map<String, String>{
        return dataStore.data.map{
            preferences ->
            val data=preferences[LOGIN_SETTING_NAME]?:""
            if (data.isEmpty())emptyMap()
            else json.decodeFromString(MapSerializer(String.serializer(), String.serializer()),data)
        }.first()
    }

}