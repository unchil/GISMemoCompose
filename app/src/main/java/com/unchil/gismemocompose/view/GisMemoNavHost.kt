package com.unchil.gismemocompose.view

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import coil3.size.Size
import com.unchil.gismemocompose.navigation.GisMemoDestinations


@Composable
fun GisMemoNavHost(
    navController: NavHostController
){

    NavHost(
        navController = navController,
        startDestination = GisMemoDestinations.IntroView.route
    ) {

        composable(
            route = GisMemoDestinations.IntroView.route
        ){
            IntroView(navController = navController)
        }

        composable(
            route = GisMemoDestinations.WriteMemoView.route
        ){
            WriteMemoView(navController = navController)
        }

        composable(
            route = GisMemoDestinations.MapView.route
        ){
            MemoMapView(navController = navController)
        }

        composable(
            route = GisMemoDestinations.SettingView.route
        ){
            SettingsView(navController = navController)
        }

        composable(
            route = GisMemoDestinations.DetailMemoView.route ,
            arguments = listOf(
                navArgument(GisMemoDestinations.ARG_NAME_ID){
                    nullable = false
                    type = NavType.StringType } )

        ){
            DetailMemoView(
                navController = navController,
                id = GisMemoDestinations.DetailMemoView.getIDFromArgs(it.arguments).toLong())
        }

        composable(
            route = GisMemoDestinations.CameraCompose.route
        ){
            CameraCompose( navController = navController)
        }

        composable(
            route = GisMemoDestinations.SpeechToText.route
        ){
            SpeechRecognizerCompose( navController = navController)
        }

        composable(
            route =  GisMemoDestinations.PhotoPreview.route,
            arguments = listOf(
                navArgument(GisMemoDestinations.ARG_NAME_FILE_PATH) {
                    nullable = false
                    type = NavType.StringType})
        ){
            ImageViewer(
                data = GisMemoDestinations.PhotoPreview.getUriFromArgs(it.arguments).toUri(),
                size = Size.ORIGINAL,
                isZoomable = true )
        }


        composable(
            route = GisMemoDestinations.ExoPlayerView.route,
            arguments = listOf(
                navArgument(GisMemoDestinations.ARG_NAME_FILE_PATH) {
                    nullable = false
                    type = NavType.StringType},
                navArgument(GisMemoDestinations.ARG_NAME_ISVISIBLE_AMPLITUDES) {
                    nullable = false
                    type = NavType.BoolType})
        ){
            ExoplayerCompose(
                uri = GisMemoDestinations.ExoPlayerView.getUriFromArgs(it.arguments).toUri(),
                isVisibleAmplitudes = GisMemoDestinations.ExoPlayerView.getIsVisibleAmplitudesFromArgs(it.arguments)
            )
        }



    }


    BackHandler {

        navController.popBackStack()
    }

}
