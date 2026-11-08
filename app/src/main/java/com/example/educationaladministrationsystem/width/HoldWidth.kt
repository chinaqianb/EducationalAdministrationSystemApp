package com.example.educationaladministrationsystem.width

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.educationaladministrationsystem.width.eASWeb.EASWeb
import com.example.educationaladministrationsystem.width.jumpRoutes.Routes
import com.composables.icons.lucide.R.drawable as Lucide

class HoldWidth {

    @Composable
    fun HoldLoad(){
        val navController= rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoutes=navBackStackEntry?.destination?.route
        Scaffold(
            floatingActionButton = {
                if (currentRoutes== Routes.EAS_WEB)
                    GoBtn{
                        navController.navigate(Routes.MY_PAGE){
                popUpTo(navController.graph.findStartDestination().id){saveState=true}
                        launchSingleTop=true
                            restoreState=true} }
            else BackBtn { navController.navigate(
                Routes.EAS_WEB){
                    popUpTo(navController.graph.findStartDestination().id){saveState=true}
                    launchSingleTop=true
                    restoreState=true} } },


            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues ->
            InitNav(paddingValues,navController)

        }
    }
    @Composable
    private fun InitNav(paddingValues: PaddingValues,navController: NavHostController) {

        NavHost(
            navController = navController,
            startDestination = Routes.EAS_WEB
        ){
            composable(Routes.EAS_WEB){
                EASWeb().InitEAS(paddingValues,true)
            }
            composable(Routes.CLASS_TABLE){

            }
            composable(Routes.MY_PAGE){
                Box(modifier = Modifier.padding(paddingValues)){
                    Text("I'm steven")
                }
            }
        }
    }
    @Composable
    private fun JumpButton(imageId: Int,str: String,onClickHandle:()-> Unit){
        ExtendedFloatingActionButton(
            onClick = {onClickHandle()},
            icon={ Icon(imageVector = ImageVector.vectorResource(imageId) ,contentDescription=str) },
            text ={ Text(str) }
        )
    }
    enum class ImageIcon(val lucideIcon: Int) {
        GO(Lucide.lucide_ic_user),
        BACK(Lucide.lucide_ic_chevron_left)
    }
    @Composable
    private fun GoBtn(onClickHandle:()-> Unit){
        JumpButton(ImageIcon.GO.lucideIcon,"我的",onClickHandle)
    }
    @Composable
    private fun BackBtn(onClickHandle:()-> Unit){
        JumpButton( ImageIcon.BACK.lucideIcon,"返回",onClickHandle)

    }

}