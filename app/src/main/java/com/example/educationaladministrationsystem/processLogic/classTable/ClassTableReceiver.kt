package com.example.educationaladministrationsystem.processLogic.classTable

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClassTableReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
       val result =goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val id=intent?.getIntExtra("courseId",-1)?:-1
                val className=intent?.getStringExtra("className")?:""
                val classPlace=intent?.getStringExtra("classPlace")?:""
                context?.let {
                    //通知并寻找下一周
                    ClassTableClock.createAndSendNotice(it,id,className,classPlace)
                    ClassTableClock.handleWeekDate(context)
                }

            }finally {
                result.finish()
            }
        }
    }
}