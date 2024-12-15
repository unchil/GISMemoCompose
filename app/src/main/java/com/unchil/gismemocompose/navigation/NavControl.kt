package com.unchil.gismemocompose.navigation

import android.net.Uri
import android.os.Bundle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ViewStream
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


sealed class GisMemoDestinations(
    val route:String,
    val name:Int = 0,
    val icon: ImageVector? = null,
){

    object MemoListScreen : GisMemoDestinations(
        route = "list",
        name = R.string.mainmenu_list,
        icon = Icons.Outlined.ViewStream
    )

    object WriteMemoScreen : GisMemoDestinations(
        route = "write",
        name = R.string.mainmenu_write,
        icon = Icons.Outlined.EditNote
    )

    object MapScreen : GisMemoDestinations(
        route = "map",
        name = R.string.mainmenu_map,
        icon = Icons.Outlined.Map
    )

    object SettingScreen : GisMemoDestinations(
        route = "setting",
        name = R.string.mainmenu_setting,
        icon = Icons.Outlined.Settings
    )

    object SpeechRecognizer : GisMemoDestinations (
        route = "speechrecognizer"
    )

    object Camera : GisMemoDestinations (
        route = "camera"
    )

    object ImageViewer : GisMemoDestinations(
        route = "imageviewer?{url}"
    ) {
        fun createRoute(filePath: Any): String {
            val path = when(filePath){
                is Int -> {
                    Uri.parse(
                        "android.resource://com.unchil.gismemo_multiplatform.android/"
                                + filePath.toString()
                    ).toString()
                }
                else -> {
                    ( filePath as Uri).encodedPath
                }
            }
            return "imageviewer?$path"
        }
        fun getUriFromArgs(bundle: Bundle?): String {
            return bundle?.getString("url") ?: ""
        }

    }

    object ExoPlayer : GisMemoDestinations(
        route = "exoplayer?{url}"
    ) {
        fun createRoute(filePath: String): String {
            return "exoplayer?$filePath"
        }
        fun getUriFromArgs(bundle: Bundle?): String {
            return  bundle?.getString("url") ?: ""
        }
    }

    object DetailMemo : GisMemoDestinations(
        route = "detailmemo?{id}"
    ){
        fun createRoute(id: Long) :String {
            return "detailmemo?$id"
        }
        fun getIDFromArgs(bundle: Bundle?): Long {
            return bundle?.getLong("id") ?: 0L
        }
    }

}