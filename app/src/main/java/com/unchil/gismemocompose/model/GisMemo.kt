package com.unchil.gismemocompose.model

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.PublishedWithChanges
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Screenshot
import androidx.compose.material.icons.outlined.Swipe
import androidx.compose.material.icons.outlined.Toll
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.unchil.gismemocompose.R
import com.unchil.gismemocompose.navigation.GisMemoDestinations


object DrawingMenuData {
    enum class Type {
        Draw,Swipe,Eraser
    }
    val Types = listOf(
        Type.Draw,
        Type.Swipe,
        Type.Eraser
    )
    fun desc(type:Type):Pair<ImageVector, Color> {
        return when(type){
            Type.Draw -> {
                Pair( Icons.Outlined.Draw , Color.Red)
            }
            Type.Swipe -> {
                Pair( Icons.Outlined.Swipe ,  Color.Red)
            }
            Type.Eraser -> {
                Pair( Icons.Outlined.Toll ,  Color.Red)
            }
        }
    }
}


object MapTypeMenuData {
    enum class Type {
        NORMAL,TERRAIN, HYBRID
    }
    val Types = listOf(
        Type.NORMAL,
        Type.TERRAIN,
        Type.HYBRID,
    )
    fun desc(type:Type):Pair<ImageVector, ImageVector?> {
        return when(type){
            Type.TERRAIN -> {
                Pair( Icons.Outlined.Forest, null)
            }
            Type.NORMAL -> {
                Pair( Icons.Outlined.Map, null)
            }
            Type.HYBRID -> {
                Pair( Icons.Outlined.Public, null)
            }
        }
    }
}

object SaveMenuData {
    enum class Type{
        CLEAR,SAVE
    }
    val Types = listOf(
        Type.CLEAR,
        Type.SAVE
    )
    fun desc(type:Type):Pair<ImageVector, ImageVector?> {
        return when(type){
            Type.CLEAR -> {
                Pair(Icons.Outlined.Replay,  null)
            }
            Type.SAVE -> {
                Pair(Icons.Outlined.PublishedWithChanges,  null)
            }
        }
    }
}


object SettingMenuData{
    enum class Type {
        SECRET, MARKER,TAG
    }
    val Types = listOf(
        Type.SECRET,
        Type.MARKER,
        Type.TAG
    )
    fun desc(type:Type):Pair<ImageVector, ImageVector?> {
        return when(type){
            Type.SECRET -> {
                Pair(Icons.Outlined.Lock,  Icons.Outlined.LockOpen)
            }
            Type.MARKER -> {
                Pair(Icons.Outlined.LocationOn,  Icons.Outlined.LocationOff)
            }
            Type.TAG -> {
                Pair(Icons.Outlined.Class,  null)
            }
        }
    }
}


object CreateMenuData {
    enum class Type {
        SNAPSHOT,RECORD,CAMERA
    }

    val Types = listOf(
        Type.SNAPSHOT,
        Type.RECORD,
        Type.CAMERA,
    )

    fun desc(type: Type):Pair<ImageVector, String?>{
        return  when(type){
            Type.SNAPSHOT -> {
                Pair(Icons.Outlined.Screenshot,  null)
            }
            Type.RECORD -> {
                Pair(Icons.Outlined.Mic,  GisMemoDestinations.SpeechToText.route)
            }
            Type.CAMERA -> {
                Pair(Icons.Outlined.Videocam, GisMemoDestinations.CameraCompose.route)
            }
        }
    }
}


sealed class MemoData {
    data class Photo(val dataList: MutableList<String>) : MemoData()
    data class SnapShot(val dataList: MutableList<String>) : MemoData()
    data class AudioText(var dataList: MutableList<Pair<String,List<String>>>) : MemoData()
    data class Video(val dataList: MutableList<String>) : MemoData()

}


enum class ListItemBackgroundAction {
    SHARE,DELETE
}

fun ListItemBackgroundAction.getDesc(): Pair<String, ImageVector>{
    return when(this){
        ListItemBackgroundAction.SHARE -> {
            Pair(this.name,  Icons.Rounded.Share)
        }
        ListItemBackgroundAction.DELETE -> {
            Pair(this.name,   Icons.Rounded.Delete)
        }
    }
}

enum class BiometricCheckType {
    DETAILVIEW, SHARE, DELETE
}

fun BiometricCheckType.getTitle(getString: (Int)->String):Pair<String,String> {
    return when(this){
        BiometricCheckType.DETAILVIEW  -> {
            Pair(getString(R.string.biometric_prompt_detailview_title), getString(R.string.biometric_prompt_detailview_msg))
        }
        BiometricCheckType.SHARE -> {
            Pair(getString(R.string.biometric_prompt_share_title), getString(R.string.biometric_prompt_share_msg))
        }
        BiometricCheckType.DELETE -> {
            Pair(getString(R.string.biometric_prompt_delete_title), getString(R.string.biometric_prompt_delete_msg))
        }
    }

}

enum class MemoDataUser {
    DetailMemoView, WriteMemoView
}

object WriteMemoData {
    enum class Type {
        PHOTO,AUDIOTEXT,VIDEO,SNAPSHOT
    }


    val Types = listOf(
        Type.SNAPSHOT,
        Type.AUDIOTEXT,
        Type.PHOTO,
        Type.VIDEO
    )

    fun desc(type: Type): Pair<Int, ImageVector>{
        return when(type){
            Type.PHOTO -> {
                Pair( R.string.dataContainer_Photo,  Icons.Outlined.Photo)
            }
            Type.AUDIOTEXT -> {
                Pair(R.string.dataContainer_AudioText,  Icons.Outlined.Mic)
            }
            Type.VIDEO -> {
                Pair(R.string.dataContainer_Video,  Icons.Outlined.Videocam)
            }
            Type.SNAPSHOT -> {
                Pair(R.string.dataContainer_Screenshot,  Icons.Outlined.Screenshot)
            }
        }
    }

}


