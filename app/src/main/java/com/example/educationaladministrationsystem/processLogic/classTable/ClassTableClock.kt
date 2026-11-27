package com.example.educationaladministrationsystem.processLogic.classTable

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.educationaladministrationsystem.R
import com.example.educationaladministrationsystem.processLogic.DataSave
import com.example.educationaladministrationsystem.processLogic.EASNotice
import com.example.educationaladministrationsystem.processLogic.classTable.ClassTableViewModel.GetClassTableListData
import com.fleeksoft.ksoup.Ksoup
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

object ClassTableClock {
   private fun callClock(context: Context,classDate: ClassTableNoticeData) {
       Log.d("clock","开始callClock")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }

        }
            alarm(alarmManager, context, classDate)

    }
    private fun alarm(alarmManager: AlarmManager,context: Context,classDate: ClassTableNoticeData){
        Log.d("clock","进入触发器")
        //触发器
        val intent = Intent(context, ClassTableReceiver::class.java).apply {
            //放入课程id,名称，上课地点
            putExtra("courseId",classDate.id)
            putExtra("className",classDate.className)
            putExtra("classPlace",classDate.classPlace)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            5,//课程id
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d("clock","id${classDate.id},name${classDate.className},place${classDate.classPlace},time${classDate.time.timeInMillis}")
        //设置触发
        //国产安卓厂商的限制，尝试用setAlarmClock
        val info = AlarmManager.AlarmClockInfo(classDate.time.timeInMillis,null)
        alarmManager.setAlarmClock(info,pendingIntent)
//        alarmManager.setExactAndAllowWhileIdle(
//            AlarmManager.RTC_WAKEUP,
//            classDate.time.timeInMillis,//多少时间后触发
//            pendingIntent
//        )
    }
    data class ClassTableNoticeData(val id: Int,
                                    val className: String,
                                    var classPlace: String,
                                    var date: String,
                                    var time: Calendar)
    private suspend fun getClassTableFromData(context: Context): Map<String, List<KbItem>> {
        val dataSave= DataSave(context)
        return dataSave.readClassData()
    }
    suspend fun getClassTableWeekDateFromData(context: Context): Map<String, List<RqazcItem>>{
        return DataSave(context).readClassWeekDate()
    }
    data class WeekData(var nowWeek: String,val allWeek: String,val weekStart: String,val weekEnd: String)
    suspend fun getWeekDataFromData(context: Context): WeekData{
        val dataSave= DataSave(context)
        val data=dataSave.read(dataSave.CLASS_TABLE_SETTING)
        val nowWeek=data.getOrDefault("now_week","")
        val allWeek=data.getOrDefault("week_num","")
        val weekStart=data.getOrDefault("week_start","")
        val weekEnd=data.getOrDefault("week_end","")
        return WeekData(nowWeek, allWeek,weekStart, weekEnd)
    }
    fun isNowWeek(weekData: WeekData): Boolean {
        val dateStart= LocalDate.parse(weekData.weekStart)
        val dateEnd= LocalDate.parse(weekData.weekEnd)
        val now=LocalDate.now()
        return !now.isBefore(dateStart)&&!now.isAfter(dateEnd)
    }
    private val dataMap=mapOf("1-2" to (8 to 10),
        "3-4" to (10 to 10),
        "5-6" to (14 to 0),
        "7-8" to (16 to 0),
        "9-10" to (18 to 40))
    suspend fun handleWeekDate(context: Context){
        //第一次调用在设置中先判断是否获取通知，闹钟权限，检查缓存中是否有内容，再决定触发
        //后续在onReceiver中寻找和触发
        Log.d("clock","开始处理")
        val map=getClassTableFromData(context)
        val weekDateMap=getClassTableWeekDateFromData(context)
        val candidates=buildList {
            for ((week,items) in map){
                val dates =weekDateMap[week].orEmpty()
                for (item in items){
                    findNext(item,dates)?.let { add(it) }
                }
            }
        }
        val next =candidates.minByOrNull { it.time.timeInMillis }?:return//找到最小
        callClock(context,next)

    }
    private fun findNext(e: KbItem,dates: List<RqazcItem>): ClassTableNoticeData?{
        val weekDay=e.xqj?.toIntOrNull()?:return null
        val dateStr =dates.firstOrNull { it.xqj==weekDay }?.rq?:return null//得到日期
        val dateParse= runCatching {  LocalDate.parse(dateStr)}.getOrNull()?:return null
        val year=dateParse.year
        val month=dateParse.monthValue
        val day=dateParse.dayOfMonth
        val (hour,minute)=dataMap[e.jcs]?:return null
        val time =classTime(hour, minute)//映射的时间
        //加上年月日
        time.set(Calendar.YEAR,year)
        time.set(Calendar.MONTH,month-1)//0-11
        time.set(Calendar.DAY_OF_MONTH,day)
        time.timeInMillis -= 60*1000*25//提前25分钟提醒
        val now=System.currentTimeMillis()
        if (time.timeInMillis<= now) {
            Log.d("clock","过期了-课程:${e.kcmc?:""},日期:${dateStr},时间:${time.timeInMillis},当前时间:${now}")
            //过期了
            return null
        }
        val id=e.jgh?.substringAfterLast("-")?.toIntOrNull()?:return null
        val one= ClassTableNoticeData(id,e.kcmc?:"",e.cdmc?:"",dateStr,time)
        return one
    }

    private fun classTime(hour:Int,minute: Int): Calendar{
        val calendar=Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY,hour)
        calendar.set(Calendar.MINUTE,minute)
        calendar.set(Calendar.SECOND,0)
        calendar.set(Calendar.MILLISECOND,0)
        return calendar
    }
    private fun createNotice(context: Context,title: String,text: String):Notification {
        val notice= NotificationCompat.Builder(context, EASNotice.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notice)
            .setColor(0x287FB9)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(false)//常住
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)//公开可见
            .setPriority(NotificationCompat.PRIORITY_LOW)
                .setTimeoutAfter(TimeUnit.MINUTES.toMillis(25))//25分钟后删除
//            .setTimeoutAfter(10000)
            .build()
        //发送
        //NotificationManagerCompat.from(application.applicationContext).notify(1201,notice)//每条通知的唯一id
        return notice
    }
    fun createAndSendNotice(context: Context, id: Int, title: String, text: String) {
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context)
                    .notify(id,createNotice(context,title,text))
            }
        }else{
            NotificationManagerCompat.from(context)
                .notify(id,createNotice(context,title,text))
        }
    }
    private suspend fun getFirstPageData(): GetClassTableListData{
        val response= ClassTableApi.service.getPage()
        val doc = Ksoup.parse(response)
        val xnm = doc.selectFirst("input[name=xnm]")?.attr("value") ?: ""
        val xqm = doc.selectFirst("input[name=xqm]")?.attr("value") ?: ""
        val zs  = doc.selectFirst("input[name=zs]")?.attr("value") ?: ""//周数
        val weekNum=doc.selectFirst("input[name=zccd]")?.attr("value")?:""
        //print("第一节课:"+data["kbList"][0]["kcmc"])
        return GetClassTableListData(xnm,xqm,zs,weekNum)
    }
    data class ErrorInfo(val isError: Boolean,val info: String)
    suspend fun getClassTableList(context: Context,
                                  callBack:((firstData:GetClassTableListData,
                                             backData: Map<String, List<KbItem>>,
                                             backWeekData:Map<String, List<RqazcItem>>)->Unit)?=null): ErrorInfo{
         Log.d("clock","同步数据中")
        if (ClassTableApi.cookieJar.isEmpty()){
            Log.d("ClassTableViewModel","cookies is empty")
            return ErrorInfo(true,"cookies是空的")
        }
        val firstData=getFirstPageData()
//        Log.d("ClassTableViewModel","获取第一份数据:${firstData}")
        if (firstData.xnm.isEmpty()||firstData.xqm.isEmpty()||firstData.nowWeek.isEmpty()){
            Log.d("ClassTableViewModel","first data is empty")
            return ErrorInfo(true,"获取初始数据是空的")
        }
        val backData= mutableMapOf<String, List<KbItem>>()
        val backWeekDate=mutableMapOf<String, List<RqazcItem>>()
        var weekStart =""
        var weekEnd =""
         val dataSave = DataSave(context)
         val old =dataSave.read(dataSave.CLASS_TABLE_SETTING)
        getNeedWeek(firstData.nowWeek,firstData.weekNum,old["get_list_num"]?.toInt()?:1).forEach { e->
            val response= ClassTableApi.service.getClassTable(firstData.xnm,firstData.xqm,e)
            if (e==firstData.nowWeek){
                weekStart=response.rqazcList[0].rq?:""
                weekEnd=response.rqazcList[6].rq?:""
            }
            backData[e]=response.kbList
            backWeekDate[e]=response.rqazcList
        }

        dataSave.save(old+mapOf("now_week" to firstData.nowWeek,"week_num" to firstData.weekNum,"week_start" to weekStart,"week_end" to weekEnd),dataSave.CLASS_TABLE_SETTING)
        dataSave.saveClassData(backData)
        dataSave.saveClassWeekDate(backWeekDate)
         Log.d("clock","同步成功")
        if (callBack!=null){
            callBack(firstData, backData,backWeekDate)
        }
        return ErrorInfo(false,"")
    }
    private fun getNeedWeek(nowWeek: String,weekNum: String,need: Int=1): List<String>{
        val now=nowWeek.toInt()
        val all=weekNum.toInt()
        val small= 1.coerceAtLeast(now - need)
        val big= (now + need).coerceAtMost(all)
        val list =mutableListOf<String>()
        for (i in small..big){
            list.add(i.toString())
        }
        Log.d("classTableViewModel",list.toString())
        return list
    }
    private suspend fun getTermData():GetClassTableListData{
        val nowTerm=getNowTerm()
        if (!nowTerm.isNowTerm){
            Log.d("Clock","未找到现在学期")
            return GetClassTableListData("","","","")
        }
        val response= ClassTableApi.service.getTermWeek(nowTerm.year,nowTerm.term)
        val nowWeek=getNowWeekByList(response)
        val allWeek=response.size
        return GetClassTableListData(nowTerm.year,nowTerm.term,nowWeek,allWeek.toString())
    }
    private fun getNowWeekByList(list:List<ResponseWeekData>): String{
        val todayDate=LocalDate.now()
        for (e in list){
            val week=e.rq
            val startDate= LocalDate.parse(week.substringBefore("/"))
            val endDate= LocalDate.parse(week.substringAfter("/"))
            if (!(todayDate.isBefore(startDate)&&todayDate.isAfter(endDate))){
                //找到当前周
                return e.zs
            }
        }
        return ""
    }
    private suspend fun getNowTerm(): TermInfo{
        //获取基于今天日期的学期信息
        val todayDate=LocalDate.now()
        val todayYear=todayDate.year//2026
        //上学期
        if (isNowTerm(todayYear.toString(),"3"))return TermInfo(true,todayYear.toString(),"3")
        //下学期
        if (isNowTerm((todayYear-1).toString(),"12"))return TermInfo(true,(todayYear-1).toString(),"12")
        //-1上学期,如2026年上学期在2027年部分
        if (isNowTerm((todayYear-1).toString(),"3"))return TermInfo(true,todayYear.toString(),"3")
        return TermInfo(false,"2026","3")
    }
    //https://jwx.gzmtu.edu.cn/jwglxt/kbcx/xskbcxMobile_cxZc.html
    //xnm 2026 xqm 3
    // list
    data class TermInfo(val isNowTerm: Boolean=false,val year: String,val term: String)
    private suspend fun isNowTerm(todayYear: String,xqm: String): Boolean{
        val todayDate=LocalDate.now()
        //只获得年份无法判断是否是第一学期3，还是第二学期12,所以要获取之后判断区间
        val response= ClassTableApi.service.getTermWeek(todayYear,xqm)
        val firstDate=response.firstOrNull()?:return false
        val lastDate=response.last()
        //拿到这学期的起始和终止日期
        val startDate= LocalDate.parse(firstDate.rq.substringBefore("/"))
        val endDate= LocalDate.parse(lastDate.rq.substringAfter("/"))
        val isTerm=!todayDate.isBefore(startDate)&&!todayDate.isAfter(endDate)
        return isTerm
    }
}