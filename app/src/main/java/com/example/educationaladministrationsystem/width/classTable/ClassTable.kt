package com.example.educationaladministrationsystem.width.classTable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educationaladministrationsystem.processLogic.classTable.ClassTableViewModel
import com.example.educationaladministrationsystem.processLogic.classTable.KbItem
import com.example.educationaladministrationsystem.processLogic.classTable.RqazcItem
import com.example.educationaladministrationsystem.width.settings.Settings
import com.composables.icons.lucide.R.drawable as Lucide

class ClassTable {
    @Composable
    fun InitClassTable(paddingValues: PaddingValues,viewModel: ClassTableViewModel= viewModel()){
        val data by viewModel.data.collectAsState()
        val backWeekData by viewModel.backWeekData.collectAsState()
        var needShowIndex by remember { mutableStateOf<KbItem?>(null) }
        val nowWeek by viewModel.nowWeek.collectAsState()
        val allWeek by viewModel.allWeek.collectAsState()
        val loading by viewModel.loading.collectAsState()
        val index by viewModel.index.collectAsState()
        val isError by viewModel.isError.collectAsState()
        var showSheet by remember { mutableStateOf(false) }
        Column(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF6F6F6)),
            verticalArrangement = Arrangement.Top
        ){

            if (loading){

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp))//转圈动画
                }
            }else{
                if (isError.isError){
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                       Text("发生了一个错误\n错误by:${isError.info}\n请退出重新加载\n或请尝试同步", fontWeight = FontWeight.Bold)
                    }
                }else {
                    TopClassTable(viewModel, data, index, nowWeek, allWeek)
                    MyClassTable(
                        viewModel,
                        data,
                        backWeekData,
                        index.toString(),
                        { needData -> needShowIndex = needData; showSheet = true })
                    if (showSheet && needShowIndex != null) {
                        needShowIndex?.let { SheetBox(showSheet, { showSheet = false }, it, backWeekData, index) }
                    }
                }
            }

        }
    }
    @Composable
    private fun TopClassTable(viewModel: ClassTableViewModel,data: Map<String, List<KbItem>>,index: Int,nowWeek: String,allWeek: String){
        val minKey: String? = data.keys.minByOrNull { it.toInt() }
        val maxKey: String? = data.keys.maxByOrNull { it.toInt() }
//        Log.d("classTable","min:${minKey},max:${maxKey},now:${index}")
        Box(modifier = Settings().SettingBoxModifier().fillMaxWidth().height(50.dp)){
            Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center){
                if (index!=1&&index!=minKey?.toInt())
                    IconButton(onClick = {viewModel.indexBack()},modifier = Modifier.align(Alignment.CenterStart))
                    { Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_chevron_left), contentDescription = "left") }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = index.toString(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (index.toString() == nowWeek) Text(
                        text = "当前周",
                        textAlign = TextAlign.Unspecified,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (index!=allWeek.toInt()&&index!=maxKey?.toInt())
                    IconButton(onClick = {viewModel.indexGo()},modifier = Modifier.align(Alignment.CenterEnd))
                    {Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_chevron_right), contentDescription = "right")}
            }
        }

    }
    data class ShowClassTable(var name: String,var info: String,val isClassElement: Boolean=true)
    @Composable
    private fun MyClassTable(viewModel: ClassTableViewModel,
                             data: Map<String, List<KbItem>>,
                             backWeekData:Map<String, List<RqazcItem>>,
                             indexZ: String,onOpen: (data: KbItem) -> Unit){
        val map= mutableListOf(
                listOf(
                ShowClassTable("","",false),
                ShowClassTable("星期一","",false),
                ShowClassTable("星期二","",false),
                ShowClassTable("星期三","",false),
                ShowClassTable("星期四","",false),
                ShowClassTable("星期五","",false),
                ShowClassTable("星期六","",false),
                ShowClassTable("星期日","",false)),
           listOf( ShowClassTable("1","08:10\n9:50",false),ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
               ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("","")),
             listOf(ShowClassTable("2","10:10\n11:50",false),ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                 ShowClassTable("",""),
                ShowClassTable("","")),
             listOf(ShowClassTable("3","14:00\n15:40",false),ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                 ShowClassTable("",""),
                ShowClassTable("","")),
             listOf(ShowClassTable("4","16:00\n17:40",false),ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                 ShowClassTable("",""),
                ShowClassTable("","")),
             listOf(ShowClassTable("5","18:40\n20:20",false) ,ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                 ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("","")),

        )
        val classTo =mapOf("1-2" to 1,"3-4" to 2,"5-6" to 3,"7-8" to 4,"9-10" to 5)
        data[indexZ]?.forEach { e->
            if (e.jcs!=null&&classTo[e.jcs]!=null&&e.xqj!=null) {
                val one: Int = classTo[e.jcs]!!
                val second: Int = e.xqj.toInt()
                map[one][second].name=e.kcmc?:""
                map[one][second].info=e.cdmc?:""
            }
        }
        val weekMap=backWeekData[indexZ]?:emptyList()
        map[0]=map[0].map { e-> e.copy(info=weekMap.firstOrNull{it.xqjmc==e.name}?.rq?:"")}
        Box(modifier = MyClassTableModifier().height(400.dp)){
            LazyColumn(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally){
                //每一行
                items(map.size){
                    line->
                    Row(modifier = Modifier.height(60.dp)){
                        repeat(map[line].size){
                            index->
                            //每一个元素
                            ClassTableItem(map[line][index]) {
                                if (map[line][index].name.isNotEmpty()) {
                                    data[indexZ]?.first { it.kcmc == map[line][index].name }
                                        ?.let { onOpen(it) }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
    @Composable
    private fun MyClassTableModifier(): Modifier{
        return Modifier
            .padding(horizontal = 5.dp)//外边距
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp),clip = false, ambientColor = Color.Black.copy(alpha = 0.1f), spotColor = Color.Black.copy(alpha = 0.1f))
            .fillMaxWidth()
//            .height(300.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
            .padding(2.dp)//内边距
    }
    @Composable
    private fun ClassTableItem(data: ShowClassTable,onOpen:()-> Unit){
        Column(modifier = if (data.isClassElement)ClassTableItemModifier().clickable { onOpen() }else ClassTableItemModifier(),
            verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally){

                Text(data.name, fontSize = if (data.isClassElement)9.sp else 11.sp)


            Text(data.info, fontSize = 7.sp)
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ){
//                Text(data.name, fontSize = if (data.isClassElement)9.sp else 15.sp)
//                Text(data.info, fontSize = 7.sp)
//            }
        }
    }
    @Composable
    private fun ClassTableItemModifier(): Modifier{
        return Modifier

            .padding(horizontal = 2.dp)//外边距
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(5.dp),clip = false, ambientColor = Color.Black.copy(alpha = 0.1f), spotColor = Color.Black.copy(alpha = 0.1f))
            .size(width = 39.dp, height = 90.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
            .padding(1.dp)//内边距
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SheetBox(show: Boolean,setClose:()-> Unit,data: KbItem, backWeekData:Map<String, List<RqazcItem>>,index: Int){
        val sheetState= rememberModalBottomSheetState(skipPartiallyExpanded = false)
        val week =backWeekData[index.toString()]?:emptyList()
        val xqj=data.xqj?.toIntOrNull()
        var date="未知日期"
//        Log.d("ClassTabel","xqj:${xqj},date:${week}")
        if (xqj!=null){
             date=week.firstOrNull{it.xqj==xqj}?.rq?:"未知日期"
        }

        ModalBottomSheet(
            onDismissRequest = {setClose()},
            sheetState = sheetState
        ){
            SheetItem("课程名称:",data.kcmc?:"")
            Spacer(modifier = Modifier.height(5.dp))
            SheetItem("上课地点:",data.cdmc?:"")
            Spacer(modifier = Modifier.height(5.dp))
            SheetItem("老师名称:",data.xm?:"")
            Spacer(modifier = Modifier.height(5.dp))
            SheetItem("课程节数:",data.jc?:"")
            Spacer(modifier = Modifier.height(5.dp))
            SheetItem("课程日期:",date)
            Spacer(modifier = Modifier.height(5.dp))
            SheetItem("上课星期:",data.xqjmc?:"")
            Spacer(modifier = Modifier.height(5.dp))
            SheetItem("课程周数:",data.zcd?:"")
            Spacer(modifier = Modifier.height(5.dp))
            SheetItem("上课班级:",data.jxbzc?:"")
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
    @Composable
    private fun SheetItem(text: String,content: String){
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
            Text(text = text)
            Text(text = content)
        }
    }
}