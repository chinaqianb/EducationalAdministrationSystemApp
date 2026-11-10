package com.example.educationaladministrationsystem.width.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.educationaladministrationsystem.width.jumpRoutes.Routes
import com.composables.icons.lucide.R.drawable as Lucide
class Settings {
    data class SettingItem(val set: String,val icon: Int,val routes:String)
    @Composable
    fun InitSettings(paddingValues: PaddingValues,onclick: (String) -> Unit){
        Column(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF6F6F6)),
            verticalArrangement = Arrangement.Top
        ){
            RenderSettings {str -> onclick(str)}
        }
    }
    @Composable
    private fun RenderSettings(onclick: (String) -> Unit){
        val mainSettingsList=listOf(SettingItem("登录设置",Lucide.lucide_ic_graduation_cap,Routes.Setting.LOGIN_SETTING),
            SettingItem("课表设置",Lucide.lucide_ic_book_text, Routes.Setting.CLASS_TABLE_SETTING),
            SettingItem("我的设置",Lucide.lucide_ic_circle_user_round, Routes.Setting.MY_USER_SETTING))
        val failSettingsList=listOf(SettingItem("版本信息",Lucide.lucide_ic_badge_info, Routes.Setting.VERSION_INFO_SETTING))
        Box(modifier = SettingBoxModifier()){
            LazyColumn(){
                items(mainSettingsList){
                    RenderListItem(it){str->onclick(str)}
                    if (it!=mainSettingsList.last())
                    HorizontalDivider(thickness = 0.5.dp,color= MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box(modifier = SettingBoxModifier()){
            LazyColumn(){
                items(failSettingsList){
                    RenderListItem(it) { str -> onclick(str) }
                    if (it!=failSettingsList.last())
                    HorizontalDivider(thickness = 0.5.dp,color= MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }

    }
    @Composable
    private fun SettingBoxModifier(): Modifier{
        return Modifier
            .padding(horizontal = 10.dp)//外边距
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp),clip = false, ambientColor = Color.Black.copy(alpha = 0.1f), spotColor = Color.Black.copy(alpha = 0.1f))
            .fillMaxWidth()
//            .height(300.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
            .padding(12.dp)//内边距
    }
    @Composable
    private fun RenderListItem(list: SettingItem,onclick:(String)-> Unit){
        ListItem(headlineContent = { Text(list.set) },
            leadingContent = { Icon(imageVector = ImageVector.vectorResource(list.icon), contentDescription = list.set) },
            trailingContent = {Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_chevron_right), contentDescription = "进入")},
            modifier = Modifier.clickable { onclick(list.routes) }
            )
    }
    @Composable
    fun RenderSettingBoxItem(elementName: String,onclick:(()-> Unit)?=null, content:@Composable ColumnScope.()-> Unit){
        Box(modifier = SettingBoxModifier().then(if (onclick!=null) Modifier.clickable { onclick() }else Modifier)){
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(elementName)
                }
                Spacer(Modifier.height(4.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(5.dp))
                Column(Modifier.fillMaxWidth()) {
                    content()
                }
                }


        }
    }

}