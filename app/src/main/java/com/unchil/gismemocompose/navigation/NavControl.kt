package com.unchil.gismemocompose.navigation

import android.net.Uri
import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.unchil.gismemocompose.R


fun NavHostController.navigateTo(route: String) =
    this.navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(
            this@navigateTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }


/*
@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.animated_composable(
    route: String,
    arguments: List<NamedNavArgument> = listOf(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
){
    val animSpec: FiniteAnimationSpec<IntOffset> = tween(500, easing = FastOutSlowInEasing)

    composable(
        route,
        arguments = arguments,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { screenWidth -> screenWidth },
                animationSpec = animSpec
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { screenWidth -> -screenWidth },
                animationSpec = animSpec
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { screenWidth -> -screenWidth },
                animationSpec = animSpec
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { screenWidth -> screenWidth },
                animationSpec = animSpec
            )
        },
        content = content
    )
}

 */


val mainScreens:List<GisMemoDestinations> = listOf(
    GisMemoDestinations.IntroView,
    GisMemoDestinations.WriteMemoView,
    GisMemoDestinations.MapView,
    GisMemoDestinations.SettingView
)

sealed class GisMemoDestinations(
    val route:String,
    val name:Int = 0,
    val icon:ImageVector? = null,

){

    object IntroView : GisMemoDestinations(
        route = "introview",
        name = R.string.mainmenu_list,
        icon = Icons.AutoMirrored.Outlined.FormatListBulleted
    )


    object WriteMemoView : GisMemoDestinations(
        route = "writememo",
        name = R.string.mainmenu_write,
        icon = Icons.Outlined.EditNote
    )


    object MapView : GisMemoDestinations(
        route = "mapview",
        name = R.string.mainmenu_map,
        icon = Icons.Outlined.Map
    )

    object SettingView : GisMemoDestinations(
        route = "settings",
        name = R.string.mainmenu_setting,
        icon = Icons.Outlined.Settings
    )

    object SpeechToText : GisMemoDestinations ( route = "voicerecording")

    object CameraCompose : GisMemoDestinations ( route = "camerapreview")

    object PhotoPreview : GisMemoDestinations( route = "photopreview?${ARG_NAME_FILE_PATH}={$ARG_NAME_FILE_PATH}") {

        fun createRoute(filePath: Any): String {
            val path = when(filePath){
                is Int -> {
                    Uri.parse("android.resource://com.example.gismemo/" + filePath.toString()).toString()
                }
                else -> {
                    ( filePath as Uri).encodedPath
                }
            }
            return "photopreview?${ARG_NAME_FILE_PATH}=${path}"
        }

        val createRouteNew: (filePath: Any) ->  String  = { filePath ->
            val path = when(filePath){
                is Int -> {
                    Uri.parse("android.resource://com.example.gismemo/" + filePath.toString()).toString()
                }
                else -> {
                    ( filePath as Uri).encodedPath
                }
            }
            "photopreview?${ARG_NAME_FILE_PATH}=${path}"
        }


        fun getUriFromArgs(bundle: Bundle?): String {
            return bundle?.getString(ARG_NAME_FILE_PATH) ?: ""
        }
    }

    object ExoPlayerView : GisMemoDestinations( route = "exoplayerview?${ARG_NAME_FILE_PATH}={$ARG_NAME_FILE_PATH}&${ARG_NAME_ISVISIBLE_AMPLITUDES}={$ARG_NAME_ISVISIBLE_AMPLITUDES}"  ) {

        fun createRoute(filePath: String, isVisibleAmplitudes:Boolean = false): String {
            return "exoplayerview?${ARG_NAME_FILE_PATH}=${filePath}&${ARG_NAME_ISVISIBLE_AMPLITUDES}=${isVisibleAmplitudes}"
        }

        fun getUriFromArgs(bundle: Bundle?): String {
            return  bundle?.getString(ARG_NAME_FILE_PATH) ?: ""
        }

        fun getIsVisibleAmplitudesFromArgs(bundle: Bundle?): Boolean {
            return  bundle?.getBoolean(ARG_NAME_ISVISIBLE_AMPLITUDES) ?: false
        }
    }

    object DetailMemoView : GisMemoDestinations(  route = "detailmemoview?${ARG_NAME_ID}={$ARG_NAME_ID}"  ) {

        fun createRoute(id: String) :String {
            return "detailmemoview?${ARG_NAME_ID}=${id}"
        }

        fun getIDFromArgs(bundle: Bundle?): String {
            return bundle?.getString(ARG_NAME_ID) ?: ""
        }

    }


    companion object {
        const val ARG_NAME_ID: String = "id"
        const val ARG_NAME_FILE_PATH: String = "url"
        const val ARG_NAME_ISVISIBLE_AMPLITUDES:String = "isvisibleamplitudes"
    }


}

