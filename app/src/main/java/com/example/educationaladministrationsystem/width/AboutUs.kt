package com.example.educationaladministrationsystem.width

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.educationaladministrationsystem.processLogic.aboutUs.AboutUsViewModel
import com.example.educationaladministrationsystem.width.settings.Settings
import com.composables.icons.lucide.R.drawable as Lucide

class AboutUs {
    @Composable
    fun InitAboutUs(paddingValues: PaddingValues,viewModel: AboutUsViewModel= viewModel()) {
        val isLoading by viewModel.isLoading.collectAsState()
        val isGet by viewModel.isGet.collectAsState()
        val context = LocalContext.current
        var showDialog by remember { mutableStateOf(false) }
        if (showDialog){
            JoinUsDialog(context, viewModel,{showDialog=false})
        }
        Column(Modifier.padding(paddingValues).fillMaxSize().background(Color(0xFFF6F6F6))
            .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
        ){
          if (isLoading){
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                  CircularProgressIndicator(modifier = Modifier.size(30.dp))//转圈动画
              }
          }else{

                  RenderAboutUs(isGet, context)
                  Spacer(Modifier.height(5.dp))
                  RenderJoinUs(context, { showDialog = true })
                  Spacer(Modifier.height(5.dp))
                  RenderRepo(context)
                  Spacer(Modifier.height(5.dp))
                  RenderLicence(context)
              }

        }
    }
    @Composable
    private fun RenderAboutUs(data: AboutUsViewModel.AuthorInfo,context: Context){
        Settings().RenderSettingBoxItem("作者信息", {openUrl(context,data.url)},{
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubcomposeAsyncImage(
                    data.authorImage, contentDescription = "作者头像",
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(30.dp))//转圈动画
                        }
                    }, error = {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("加载失败")
                        }
                    }, contentScale = ContentScale.Crop,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(40.dp)))
                Text(data.name, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }
        })
    }
    fun openUrl(context: Context, url: String){
        try {
            val intent= Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        }catch (e: ActivityNotFoundException){
            Toast.makeText(context,"没有浏览器打开", Toast.LENGTH_SHORT).show()
        }
    }
    @Composable
    private fun RenderJoinUs(context: Context,onOpen:()-> Unit){

            Row(modifier = Settings().SettingBoxModifier().clickable { onOpen() }, horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically){
                Text("反馈bug与交流")
                Text("加群", color = Color.Gray)
            }

    }
    @Composable
    private fun JoinUsDialog(context: Context,viewModel: AboutUsViewModel,onClose:()-> Unit){
        AlertDialog(
            onDismissRequest = {onClose()},
            title = {Text("反馈与交流")},
            text = {Text("qq群号:1106570018")},
            confirmButton = {TextButton(onClick = {viewModel.addGroup(context);onClose()}){Text("加群")} },
            dismissButton = { TextButton(onClick = {onClose()}){Text("取消")}}
        )
    }
    @Composable
    private fun RenderRepo(context: Context){
        val repoUrl ="https://github.com/chinaqianb/EducationalAdministrationSystemApp"
        Settings().RenderSettingBoxItem("仓库地址",{openUrl(context,repoUrl)},{
            Spacer(Modifier.height(10.dp))

            Text(buildAnnotatedString { append("仓库地址:")
            withLink(LinkAnnotation.Url(
                url = repoUrl,
                styles = TextLinkStyles( SpanStyle(
                    textDecoration = TextDecoration.Underline)
            ))){append(repoUrl)}
            })

        })
    }
    @Composable
    private fun RenderLicence(context: Context){
        Settings().RenderSettingBoxItem("许可证", content = {

            Spacer(Modifier.height(10.dp))
            Row {
                Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_creative_commons), contentDescription = "cc")
//                Text("本软件使用的是", modifier =  Modifier.alignByBaseline())
//                Text("CC BY-NC-ND 4.0 ", fontWeight = FontWeight.Bold, modifier = Modifier.alignByBaseline()
//                    .clickable { openUrl(context,"https://creativecommons.org/licenses/by-nc-nd/4.0/legalcode.txt") })
                Text(buildAnnotatedString { append("本软件使用的是")
                    withLink(LinkAnnotation.Url(
                        url = "https://creativecommons.org/licenses/by-nc-nd/4.0/legalcode.txt",
                        styles = TextLinkStyles( SpanStyle(
                            fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                        ))){append("CC BY-NC-ND 4.0 ")}
                    append("许可证")
                })
//                Text("许可证", modifier = Modifier.alignByBaseline())
            }
            Spacer(Modifier.height(10.dp))
            Row {
                Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_share_2), contentDescription = "share")
                Text("共享-在任何媒介以任何形式复制、发行本作品")
            }
            Spacer(Modifier.height(10.dp))
            Row {
                Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_shield_user), contentDescription = "checkName")
                Text("署名 — 您必须给出适当的署名，提供指向本许可协议的链接，同时标明是否（对原始作品）作了修改。您可以用任何合理的方式来署名，但是不得以任何方式暗示许可人为您或您的使用背书")
            }
            Spacer(Modifier.height(10.dp))
            Row {
                Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_circle_dollar_sign), contentDescription = "noCommit")
                Text("非商业性使用 — 您不得将本作品用于商业目的")
            }
            Spacer(Modifier.height(10.dp))
            Row {
                Icon(imageVector = ImageVector.vectorResource(Lucide.lucide_ic_circle_equal), contentDescription = "noChange")
                Text("禁止演绎 — 如果您 再混合、转换、或者基于该作品创作，您不可以分发修改作品")
            }
            Spacer(Modifier.height(60.dp))
        })
    }
}