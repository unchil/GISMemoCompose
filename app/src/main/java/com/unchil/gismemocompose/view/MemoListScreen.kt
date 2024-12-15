package com.unchil.gismemocompose.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.unchil.gismemocompose.LocalUsableHaptic
import com.unchil.gismemocompose.data.LocalRepository
import com.unchil.gismemocompose.data.RepositoryProvider
import com.unchil.gismemocompose.db.LuckMemoDB
import com.unchil.gismemocompose.model.SnackBarChannelObject
import com.unchil.gismemocompose.shared.composables.CheckPermission
import com.unchil.gismemocompose.shared.composables.LocalPermissionsManager
import com.unchil.gismemocompose.shared.composables.PermissionRequiredCompose
import com.unchil.gismemocompose.shared.composables.PermissionsManager
import com.unchil.gismemocompose.ui.theme.GISMemoTheme
import com.unchil.gismemocompose.viewmodel.MemoListViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow


@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class)
@Composable
fun MemoListScreen(
    navController: NavHostController
){
    val permissions = listOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val multiplePermissionsState = rememberMultiplePermissionsState( permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)
    var isGranted by mutableStateOf(true)
    permissions.forEach { chkPermission ->
        isGranted =   isGranted && multiplePermissionsState.permissions.find {
            it.permission == chkPermission
        }?.status?.isGranted ?: false
    }

    PermissionRequiredCompose(
        isGranted = isGranted,
        multiplePermissions = permissions
    ) {

        val context = LocalContext.current
        val repository = LocalRepository.current

        val coroutineScope = rememberCoroutineScope()
        val isUsableHaptic = LocalUsableHaptic.current
        val hapticFeedback = LocalHapticFeedback.current


        val viewModel = remember {
            MemoListViewModel( repository = repository)
        }
        val result = viewModel.memoPagingStream.collectAsLazyPagingItems()



        val isSearchRefreshing: MutableState<Boolean> = rememberSaveable {
            mutableStateOf(false)
        }
        val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed)
        val configuration = LocalConfiguration.current
        val isPortrait = remember { mutableStateOf(false) }
        val peekHeight = remember { mutableStateOf(140.dp) }
        val headerHeight  = remember { mutableStateOf(74.dp) }
        when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                isPortrait.value = true
            }
            else ->{
                isPortrait.value = false
            }
        }
        val channel = remember { Channel<Int>(Channel.CONFLATED) }
        val snackBarHostState = remember { SnackbarHostState() }

        LaunchedEffect(channel) {
            channel.receiveAsFlow().collect { index ->
                val channelInfo = SnackBarChannelObject.entries.first { item ->
                    item.channel == index
                }
                //----------
                val message = when (channelInfo.channelType) {
                    SnackBarChannelObject.Type.SEARCH_RESULT -> {
                        context.resources.getString( channelInfo.message) + "[${result.itemCount}]"
                    }
                    else -> {
                        context.resources.getString( channelInfo.message)
                    }
                }
                //----------
                val snackBar = snackBarHostState.showSnackbar(
                    message =   message,
                    actionLabel = channelInfo.actionLabel,
                    withDismissAction = channelInfo.withDismissAction,
                    duration = channelInfo.duration
                )
                when (snackBar) {
                    SnackbarResult.ActionPerformed -> {
                        hapticProcessing(coroutineScope, hapticFeedback, isUsableHaptic)
                        //----------
                        when (channelInfo.channelType) {
                            else -> {}
                        }
                        //----------
                    }
                    SnackbarResult.Dismissed -> {
                        hapticProcessing(coroutineScope, hapticFeedback, isUsableHaptic)
                    }
                }
            }
        }

        val snackBarHost : @Composable (androidx.compose.material.SnackbarHostState) -> Unit = { _ ->
            SnackbarHost(hostState = snackBarHostState) { snackBarData ->
                Snackbar(
                    snackbarData = snackBarData,
                    modifier = Modifier,
                    shape = ShapeDefaults.ExtraSmall,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    dismissActionContentColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }


        val appBar :@Composable () -> Unit = {
            WeatherContent(isSticky = false)
        }

        val backLayerContent :@Composable () -> Unit = {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                MemoListCompose(
                    navController = navController,
                    viewModel = viewModel,
                    scaffoldState = scaffoldState,
                    channel = channel
                )
            }
        }

        val frontLayerContent :@Composable () -> Unit = {
            SearchCompose(onEvent = viewModel.eventHandler)
        }


        BackdropScaffold(
            scaffoldState = scaffoldState,
            peekHeight = peekHeight.value,
            headerHeight = headerHeight.value,
            persistentAppBar =  false,
            backLayerBackgroundColor = Color.Transparent,
            frontLayerShape =  ShapeDefaults.Medium,
            frontLayerBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            frontLayerScrimColor = Color.Transparent,
            snackbarHost =  snackBarHost,
            appBar = appBar,
            backLayerContent = backLayerContent,
            frontLayerContent = frontLayerContent
        )




    }
}





@Preview
@Composable
fun PrevMemoListScreen() {
    val context = LocalContext.current
    val permissionsManager = PermissionsManager()
    val navController = rememberNavController()
    val luckMemoDB = LuckMemoDB.getInstance(context.applicationContext)
    val repository = RepositoryProvider.getRepository().apply { database = luckMemoDB }
    CompositionLocalProvider(
        LocalPermissionsManager provides permissionsManager,
        LocalRepository provides repository
    ) {
        GISMemoTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MemoListScreen(navController = navController)
            }
        }
    }

}
