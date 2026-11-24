package com.example.educationaladministrationsystem.width.myPage

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.educationaladministrationsystem.processLogic.DataSave
import com.example.educationaladministrationsystem.processLogic.myPage.MyPageViewModel
import com.example.educationaladministrationsystem.width.jumpRoutes.Routes
import java.io.File
import com.composables.icons.lucide.R.drawable as Lucide
class MyPage {
    @Composable
    fun InitMyPage(paddingValues: PaddingValues,viewModel: MyPageViewModel= viewModel(),onPage: (String) -> Unit){
        val imageUri by viewModel.imageUri.collectAsState()
        val userName by viewModel.userName.collectAsState()
        val loading by viewModel.loading.collectAsState()
        //viewModel.getSaveFromData()
        Column(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF6F6F6)),
            verticalArrangement = Arrangement.Top
        ){
            UserInf(imageUri, userName,viewModel,loading)
            Spacer(modifier = Modifier.height(5.dp))
            BottomList{str->onPage(str)}
        }

    }
    @Composable
    private fun UserInf(imageUri: String,userName: String,viewModel: MyPageViewModel,loading: Boolean){

        Box(modifier=UserInfModifier()) {
            if (loading){
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp))//转圈动画
                }
            }else {
                val image = viewModel.getImageFile(imageUri)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (viewModel.isImageFileExist(imageUri) && image.exists()) {
                        SubcomposeAsyncImage(
                            model = image,
                            contentDescription = "用户头像",
                            loading = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(30.dp))//转圈动画
                                }
                            },
                            error = {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Text("加载失败")
                                }
                            },
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(40.dp))
                        )
                    } else {
                        Image(
                            painter = painterResource(Lucide.lucide_ic_user),
                            contentDescription = "默认头像",
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(40.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Bold,modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
            }
        }
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
            .padding(10.dp)
    }
    @Composable
    private fun BottomList(onPage:(String)-> Unit){
        Box(modifier = BottomListModifier()){
            LazyColumn(modifier = Modifier.fillMaxSize()){
                val list =listOf(BottomListElement("设置",Lucide.lucide_ic_settings, Routes.MyPage.SETTING),
                    BottomListElement("课表",Lucide.lucide_ic_square_library, Routes.CLASS_TABLE),
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