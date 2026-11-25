package com.example.educationaladministrationsystem.width.welcome

import android.icu.text.CaseMap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educationaladministrationsystem.R
import com.example.educationaladministrationsystem.processLogic.welcome.WelcomePageViewModel
import com.composables.icons.lucide.R.drawable as Lucide

class FirstPageWelcome {
    @Composable
    fun InitFirstPage(onFinishAll:()-> Unit, viewModel: WelcomePageViewModel= viewModel()){
        val current by viewModel.currentPage.collectAsState()
        val data by viewModel.data.collectAsState()
        val loading by viewModel.isLoading.collectAsState()
        val isHashError by viewModel.isHashError.collectAsState()
        Scaffold(modifier = Modifier.background(Color(0xFFF6F6F6)),
            bottomBar = {
                BottomGoAndBack(viewModel,current)
            }

        ){ paddingValues ->
            if (current=="0"){
                WelcomePage(paddingValues,viewModel)
            }else if (current=="1"){
                if (loading){
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(30.dp))//转圈动画
                    }
                }else {
                    UrlAndNamePasswordSetting(paddingValues, viewModel, data, isHashError)
                }
            }else if (current=="2"){
                AllFinallyPage(paddingValues,viewModel,{onFinishAll()})
            }


        }
    }
    @Composable
    private fun WelcomePage(paddingValues: PaddingValues,viewModel: WelcomePageViewModel){
        val size= 25.sp
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize(), contentAlignment = Alignment.Center ){
            Column(horizontalAlignment = Alignment.CenterHorizontally){
                Text("欢迎使用",fontSize = size, fontWeight = FontWeight.Bold)
                Text("教务系统",fontSize = size, fontWeight = FontWeight.Bold, color = Color(0xFF297FBA))
                Image(painter = painterResource(R.drawable.icon),
                    contentDescription = "app_icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(150.dp))
                Text("作者 by Chinaqianb", fontSize = size, fontWeight = FontWeight.Bold)
                Text("仅供学习和交流", fontSize = size, fontWeight = FontWeight.Bold)
                Text("非商业性", fontSize = size, fontWeight = FontWeight.Bold)
                Text("许可证", fontSize = size, fontWeight = FontWeight.Bold)
                Text("CC BY-NC-ND 4.0", fontSize = size, fontWeight = FontWeight.Bold)

            }

        }
    }
    @Composable
    private fun UrlAndNamePasswordSetting(paddingValues: PaddingValues,viewModel: WelcomePageViewModel,data: WelcomePageViewModel.LoginData,isHashError: Boolean){
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.Center ) {
            Text("为了正常使用,我们需要进行一些基本设置\n(我们不会收集你的信息,所有数据均保存在本地)")

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(){
                        Text("学号")
                        Text("填写你的教务系统的学号", fontSize = 12.sp, color = Color.Gray)
                    }
                    TextField(
                        value = data.username,
                        onValueChange = { value ->
                            viewModel.putData(
                                WelcomePageViewModel.LoginData(
                                    value,
                                    data.password,
                                    data.url,
                                    data.isJsOn
                                )
                            )
                        },
                        label = { Text("学号") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(){
                        Text("密码")
                        Text("填写你教务系统的密码", fontSize = 12.sp, color = Color.Gray)
                    }
                    TextField(
                        value = data.password,
                        onValueChange = { value ->
                            viewModel.putData(
                                WelcomePageViewModel.LoginData(
                                    data.username,
                                    value,
                                    data.url,
                                    data.isJsOn
                                )
                            )
                        },
                        label = { Text("密码") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),

                ) {
                    Column(){
                        Text("网址")
                        Text("填写教务系统的网址", fontSize = 12.sp, color = Color.Gray)
                        if (isHashError)
                            Text("此应用仅适用于本校学生,为了防止数据泄漏,不对外开放。" +
                                    "\n如果你本校学生,请核对教务系统登录网址是否正确\n" +
                                    "正确网址应为https://xxx.cn/xxx/xxx.html格式", fontSize = 12.sp, color = Color.Red)
                    }
                    TextField(
                        value = data.url,
                        onValueChange = { value ->
                            viewModel.putData(
                                WelcomePageViewModel.LoginData(
                                    data.username,
                                    data.password,
                                    value,
                                    data.isJsOn
                                )
                            )
                        },
                        label = { Text("网址") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = isHashError,
                        supportingText = {if (isHashError)Text("教务系统网址错误")}
                    )
                }
                Text("本软件使用js注入来实现自动登录教务系统,在确保你的教务系统学号和密码无误的前提下请启用js注入功能\n" +
                        "如果未输入或不确保学号和密码正确,请不要打开此功能,稍后可在设置->登录设置中修改",fontSize = 12.sp, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                        Text("js注入")

                    Switch(
                        checked = data.isJsOn,
                        onCheckedChange = { value ->
                            viewModel.putData(
                                WelcomePageViewModel.LoginData(
                                    data.username,
                                    data.password,
                                    data.url,
                                    value
                                )
                            )
                        }
                    )
                }



        }

    }
    @Composable
    private fun AllFinallyPage(paddingValues: PaddingValues, viewModel: WelcomePageViewModel,onFinishAll: () -> Unit){
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center ) {

            Text("你已经完成所有设置", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("点击完成可跳转到首页",fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Button(onClick = {onFinishAll()}, shape = RoundedCornerShape(1.dp), modifier = Modifier.zIndex(2f)){Text("完成")}

        }
    }
    @Composable
    private fun BottomGoAndBack(viewModel: WelcomePageViewModel,current: String){
        Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp), contentAlignment = Alignment.Center) {
            if (current!="0") {
                TextButton(onClick = { viewModel.setPage((current.toInt()-1).toString()) }, modifier = Modifier.align(Alignment.CenterStart))
                {

                    Icon(
                        imageVector = ImageVector.vectorResource(Lucide.lucide_ic_chevron_left),
                        contentDescription = "left"
                    )
                    Text("上一步")

                }
            }
            if (current!="2") {
                TextButton(onClick = { if (current=="1")viewModel.checkAndSaveData()else viewModel.setPage("1") }, modifier = Modifier.align(Alignment.CenterEnd))
                {

                    Text("下一步")
                    Icon(
                        imageVector = ImageVector.vectorResource(Lucide.lucide_ic_chevron_right),
                        contentDescription = "right"
                    )
                }
            }

        }
    }
}