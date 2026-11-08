package com.example.educationaladministrationsystem.width.myPage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.educationaladministrationsystem.width.jumpRoutes.Routes
import com.composables.icons.lucide.R.drawable as Lucide
class MyPage {
    @Composable
    fun InitMyPage(paddingValues: PaddingValues,onPage: (String) -> Unit){
        Column(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF6F6F6)),
            verticalArrangement = Arrangement.Top
        ){
            UserInf()
            Spacer(modifier = Modifier.height(5.dp))
            BottomList (){str->onPage(str)}
        }

    }
    @Composable
    private fun UserInf(){
        Box(modifier=UserInfModifier())
    }
    @Composable
    private fun UserInfModifier(): Modifier{
        return Modifier
            .padding(horizontal = 10.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp),clip = false, ambientColor = Color.Black.copy(alpha = 0.1f), spotColor = Color.Black.copy(alpha = 0.1f))
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.7.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(10.dp))
    }
    @Composable
    private fun BottomList(onPage:(String)-> Unit){
        Box(modifier = BottomListModifier()){
            LazyColumn(modifier = Modifier.fillMaxSize()){
                val list =listOf(BottomListElement("设置",Lucide.lucide_ic_settings, Routes.MyPage.SETTING),
                    BottomListElement("关于我们",Lucide.lucide_ic_user_star, Routes.MyPage.ABOUT_US))
                items(list){
                    LoadListItem(it) { str -> onPage(str) }
                    HorizontalDivider(thickness = 0.5.dp,color= MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
    @Composable
    private fun BottomListModifier(): Modifier{
        return Modifier
            .padding(horizontal = 10.dp)//外边距
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp),clip = false, ambientColor = Color.Black.copy(alpha = 0.1f), spotColor = Color.Black.copy(alpha = 0.1f))
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.7.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
            .padding(12.dp)//内边距
    }
    data class BottomListElement(val name: String,val icon: Int,val targetRoutes: String)
    @Composable
    private fun LoadListItem(element: BottomListElement,click:(String)-> Unit){
        ListItem(
            headlineContent = { Text(element.name) },//列表项内标题内容
            leadingContent = { Icon(imageVector = ImageVector.vectorResource(element.icon), contentDescription =element.name) },//列表项的前导内容
//            supportingContent = {Text("?")},//列表项辅助内容
            trailingContent = {Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_chevron_right), contentDescription = "进入")},//尾随元素
            modifier = Modifier.clickable { click(element.targetRoutes) }//可点击
        )
    }
}