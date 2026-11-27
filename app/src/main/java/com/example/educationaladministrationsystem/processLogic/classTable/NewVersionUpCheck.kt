package com.example.educationaladministrationsystem.processLogic.classTable

import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.educationaladministrationsystem.processLogic.DataSave

object NewVersionUpCheck {
    private fun getNowVersionCode(context: Context): Long{
        val pm = context.packageManager
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(context.packageName, 0)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
    }
    fun checkIfNew(context: Context,onCallBack:()-> Unit){
        val newVersionCode=getNowVersionCode(context)
        val sp=context.getSharedPreferences("app_update", Context.MODE_PRIVATE)//轻量级配置
        val lastVersionCode=sp.getLong("lastVersionCode",-1L)
        Log.d("newVersionUp","save:${lastVersionCode},new:${newVersionCode}")
        //首次
        if (lastVersionCode==-1L){
            //放入版本code
            sp.edit(commit = true) { putLong("lastVersionCode", newVersionCode) }
            return
        }
        //更新后首次启动
        if (newVersionCode>lastVersionCode){
            //重新注册提醒
            onCallBack()
        }
        sp.edit(commit = true) { putLong("lastVersionCode", newVersionCode) }

    }
    private fun checkNoticePermission(context: Context): Boolean{
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ){
                return false
            }
        }

        return true
    }
    private fun checkHasClockPermission(context: Context): Boolean{
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return false
            }
        }
        return true
    }
    suspend fun onHandleClockRegister(context: Context){
        Log.d("newVersionUpCall","clock check reRegister")
        if (!(checkNoticePermission(context) && checkHasClockPermission(context))){
            //如果没有权限返回
            Log.d("newVersionUpCall","no permission back")
            return
            }
        val dataSave = DataSave(context)
        val data =dataSave.read(dataSave.CLASS_TABLE_SETTING)
        val saveOnNotice=data["is_on_notice"]=="1"
        val saveOnClock=data["is_on_clock"]=="1"
        if (!(saveOnNotice&&saveOnClock)){
            Log.d("newVersionUpCall","no switch open back")
            return
        }
        ClassTableClock.handleWeekDate(context)
        Log.d("newVersionUpCall","reRegisterSuccess")
    }
}