package com.example.educationaladministrationsystem.width.classTable

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educationaladministrationsystem.processLogic.classTable.ClassTableViewModel
import com.example.educationaladministrationsystem.processLogic.classTable.KbItem
import com.example.educationaladministrationsystem.processLogic.settings.SettingsViewModel
import com.example.educationaladministrationsystem.width.settings.Settings
import com.composables.icons.lucide.R.drawable as Lucide

class ClassTable {
    @Composable
    fun InitClassTable(paddingValues: PaddingValues,viewModel: ClassTableViewModel= viewModel()){
        val data by viewModel.data.collectAsState()
        var needShowIndex by remember { mutableStateOf<KbItem?>(null) }
        val nowWeek by viewModel.nowWeek.collectAsState()
        val allWeek by viewModel.allWeek.collectAsState()
        val loading by viewModel.loading.collectAsState()
        val index by viewModel.index.collectAsState()
        var showSheet by remember { mutableStateOf(false) }
        Column(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF6F6F6)),
            verticalArrangement = Arrangement.Top
        ){

            if (loading){

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp))//转圈动画
                }
            }else{
                TopClassTable(viewModel,data,index,nowWeek,allWeek)
                MyClassTable(viewModel,data,index.toString(),{needData ->needShowIndex=needData ;showSheet=true })
                if (showSheet&&needShowIndex!=null){
                    needShowIndex?.let { SheetBox(showSheet,{showSheet=false},it) }
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
                if (index!=1&&index!=minKey?.toInt()) IconButton(onClick = {viewModel.indexBack()},modifier = Modifier.align(Alignment.CenterStart)){ Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_chevron_left), contentDescription = "left") }
                Text(text = index.toString(),textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
                if (index!=allWeek.toInt()&&index!=maxKey?.toInt())IconButton(onClick = {viewModel.indexGo()},modifier = Modifier.align(Alignment.CenterEnd)){Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_chevron_right), contentDescription = "right")}
            }
            //Button(onClick = { viewModel.tryGetList() }){ Text("测试") }
        }

    }
    data class ShowClassTable(var name: String,var info: String,val isClassElement: Boolean=true)
    @Composable
    private fun MyClassTable(viewModel: ClassTableViewModel, data: Map<String, List<KbItem>>, indexZ: String,onOpen: (data: KbItem) -> Unit){
        val map=listOf(
                listOf(
                ShowClassTable("","",false),
                ShowClassTable("周一","",false),
                ShowClassTable("周二","",false),
                ShowClassTable("周三","",false),
                ShowClassTable("周四","",false),
                ShowClassTable("周五","",false),
                ShowClassTable("周六","",false),
                ShowClassTable("周日","",false)),
           listOf( ShowClassTable("1","08:10-9:50",false),ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
               ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("","")),
             listOf(ShowClassTable("2","10:10-11:50",false),ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                 ShowClassTable("",""),
                ShowClassTable("","")),
             listOf(ShowClassTable("3","14:00-15:40",false),ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                 ShowClassTable("",""),
                ShowClassTable("","")),
             listOf(ShowClassTable("4","16:00-17:40",false),ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                 ShowClassTable("",""),
                ShowClassTable("","")),
             listOf(ShowClassTable("5","18:40-20:20",false) ,ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("",""),
                 ShowClassTable("",""),
                ShowClassTable("",""),
                ShowClassTable("","")),

        )
        val classTo =mapOf("1-2" to 1,"3-4" to 2,"5-6" to 3,"7-8" to 4,"9-10" to 5)
//        Log.d("classTable",data.toString())
        data[indexZ]?.forEach { e->
            if (e.jcs!=null&&classTo[e.jcs]!=null&&e.xqj!=null) {
                val one: Int = classTo[e.jcs]!!
                val second: Int = e.xqj.toInt()
                map[one][second].name=e.kcmc?:""
                map[one][second].info=e.cdmc?:""
            }
        }
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
        Column(modifier = if (data.isClassElement)ClassTableItemModifier().clickable { onOpen() }else ClassTableItemModifier(), verticalArrangement = Arrangement.Center){
            Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.Center) {
                Text(data.name, fontSize = if (data.isClassElement)9.sp else 15.sp)
            }//15

            Text(data.info, fontSize = 7.sp)
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
    private fun SheetBox(show: Boolean,setClose:()-> Unit,data: KbItem){
        val sheetState= rememberModalBottomSheetState(skipPartiallyExpanded = false)
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
            SheetItem("课程日期:",data.dateDigit?:"")
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