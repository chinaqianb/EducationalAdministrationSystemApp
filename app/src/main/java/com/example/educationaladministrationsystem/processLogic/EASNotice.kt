package com.example.educationaladministrationsystem.processLogic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

object EASNotice {
    const val CHANNEL_ID = "eas_classTable_Notice"
    fun createNoticeChannel(context: Context){
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            val import= NotificationManager.IMPORTANCE_LOW
            val channel= NotificationChannel(CHANNEL_ID,"教务系统课表",import)
                .apply {
                    setSound(null, null)//没声音
                    enableVibration(false)//关闭震动

                }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    /**
     * 注册获取权限结果返回函数处理
     */
    fun noticeRequestPermissionCallBack(result: Boolean){
        if (result){
            //同意权限

        }else{
            //拒绝权限

        }
    }
}