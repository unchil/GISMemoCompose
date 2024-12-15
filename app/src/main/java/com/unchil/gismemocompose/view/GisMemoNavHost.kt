package com.unchil.gismemocompose.view

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import coil3.size.Size
import com.unchil.gismemocompose.navigation.GisMemoDestinations


@Composable
fun GisMemoNavHost (
    navController: NavHostController
){
    NavHost(
        navController = navController,
        startDestination = GisMemoDestinations.MemoListScreen.route
    ){

        composable(
            route = GisMemoDestinations.MemoListScreen.route
        ){
            MemoListScreen(navController = navController)
        }

        composable(
            route = GisMemoDestinations.WriteMemoScreen.route
        ){
            WriteMemoScreen(navController = navController)
        }

        composable(
            route = GisMemoDestinations.MapScreen.route
        ){
            MemoMapScreen(navController = navController)
        }

        composable(
            route = GisMemoDestinations.DetailMemo.route,
            arguments = listOf(
                navArgument("id"){
                    nullable = false
                    type = NavType.LongType
                }
            )
        ){
            DetailMemoCompose(
                navController = navController,
                id = GisMemoDestinations.DetailMemo.getIDFromArgs(it.arguments)
            )
        }


        composable(
            route = GisMemoDestinations.SettingScreen.route
        ){
            SettingsScreen(navController = navController)
        }


        composable(
            route = GisMemoDestinations.Camera.route
        ){
            CameraCompose(navController = navController)
        }

        composable(
            route = GisMemoDestinations.SpeechRecognizer.route
        ){
            SpeechRecognizerCompose(navController = navController)
        }

        composable(
            route = GisMemoDestinations.ImageViewer.route,
            arguments = listOf(
                navArgument("url") {
                    nullable = false
                    type = NavType.StringType
                }
            )
        ){
            ImageViewer(
                data = GisMemoDestinations.ImageViewer.getUriFromArgs(it.arguments),
                size = Size.ORIGINAL
            )
        }

        composable(
            route = GisMemoDestinations.ExoPlayer.route,
            arguments = listOf(
                navArgument("url"){
                    nullable = false
                    type = NavType.StringType
                }
            )
        ){
            ExoplayerCompose(
                uri = GisMemoDestinations.ExoPlayer.getUriFromArgs(it.arguments)
            )
        }



    }

    BackHandler {
        navController.popBackStack()
    }
}