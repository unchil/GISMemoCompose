package com.unchil.gismemocompose.model

import androidx.compose.material3.SnackbarDuration
import com.unchil.gismemocompose.R

object SnackBarChannelObject {
    enum class Type {
        ALL_DATA_DELETE,
        BIOMETRIC_NO_SUCCESS,
        LOCATION_SERVICE_DISABLE,
        AUTHENTICATION_FAILED,
        ITEM_DELETE,
        MARKER_CHANGE_SET,
        MARKER_CHANGE_FREE,
        LOCK_CHANGE_SET,
        LOCK_CHANGE_FREE,
        SNAPSHOT_RESULT,
        SEARCH_CLEAR,
        SEARCH_RESULT,
        MEMO_SAVE,
        MEMO_CLEAR_REQUEST,
        MEMO_CLEAR_RESULT,
        MEMO_DELETE
    }

    data class ChannelInfo(
        val channelType: Type,
        val channel:Int,
        var message:Int,
        val duration: SnackbarDuration,
        val actionLabel:String?,
        val withDismissAction:Boolean,
    )

    val entries:List<ChannelInfo> = listOf(

        ChannelInfo(
            channelType = Type.ALL_DATA_DELETE,
            channel = 13,
            message = R.string.snackbar_13,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),


        ChannelInfo(
            channelType = Type.BIOMETRIC_NO_SUCCESS,
            channel = 12,
            message = R.string.snackbar_12,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),


        ChannelInfo(
            channelType = Type.LOCATION_SERVICE_DISABLE,
            channel = 11,
            message = R.string.snackbar_11,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),


        ChannelInfo(
            channelType = Type.AUTHENTICATION_FAILED,
            channel = 10,
            message = R.string.snackbar_10,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),


        ChannelInfo(
            channelType = Type.ITEM_DELETE,
            channel = 9,
            message = R.string.snackbar_9,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),


        ChannelInfo(
            channelType = Type.MEMO_DELETE,
            channel = 8,
            message = R.string.snackbar_8,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),


        ChannelInfo(
            channelType = Type.MARKER_CHANGE_SET,
            channel = 700,
            message = R.string.snackbar_700,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),



        ChannelInfo(
            channelType = Type.MARKER_CHANGE_FREE,
            channel = 7,
            message = R.string.snackbar_7,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),


        ChannelInfo(
            channelType = Type.LOCK_CHANGE_SET,
            channel = 600,
            message = R.string.snackbar_600,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),


        ChannelInfo(
            channelType = Type.LOCK_CHANGE_FREE,
            channel = 6,
            message = R.string.snackbar_6,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),



        ChannelInfo(
            channelType = Type.SNAPSHOT_RESULT,
            channel = 5,
            message = R.string.snackbar_5,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),


        ChannelInfo(
            channelType = Type.MEMO_SAVE,
            channel = 4,
            message = R.string.snackbar_4,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),


        ChannelInfo(
            channelType = Type.SEARCH_RESULT,
            channel = 3,
            message = R.string.snackbar_3,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),


        ChannelInfo(
            channelType = Type.SEARCH_CLEAR,
            channel = 2,
            message = R.string.snackbar_2,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),

        ChannelInfo(
            channelType = Type.MEMO_CLEAR_RESULT,
            channel = 1,
            message = R.string.snackbar_1,
            duration = SnackbarDuration.Short,
            actionLabel = null,
            withDismissAction = true,
        ),

        ChannelInfo(
            channelType = Type.MEMO_CLEAR_REQUEST,
            channel = 0,
            message = R.string.snackbar_0,
            duration = SnackbarDuration.Indefinite,
            actionLabel = "Ok",
            withDismissAction = true,
        ),
    )


}