package com.unchil.gismemocompose.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Point
import android.location.Location
import android.speech.RecognizerIntent
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Api
import androidx.compose.material.icons.outlined.BedtimeOff
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.ModeOfTravel
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.widgets.ScaleBar
import com.unchil.gismemocompose.LocalUsableDarkMode
import com.unchil.gismemocompose.LocalUsableHaptic
import com.unchil.gismemocompose.R
import com.unchil.gismemocompose.data.LocalRepository
import com.unchil.gismemocompose.db.entity.CURRENTLOCATION_TBL
import com.unchil.gismemocompose.model.CreateMenuData
import com.unchil.gismemocompose.model.DrawingMenuData
import com.unchil.gismemocompose.model.MapTypeMenuData
import com.unchil.gismemocompose.model.SaveMenuData
import com.unchil.gismemocompose.model.SettingMenuData
import com.unchil.gismemocompose.model.SnackBarChannelObject
import com.unchil.gismemocompose.model.TagInfoDataObject
import com.unchil.gismemocompose.model.WriteMemoData
import com.unchil.gismemocompose.shared.ChkNetWork
import com.unchil.gismemocompose.shared.checkInternetConnected
import com.unchil.gismemocompose.shared.composables.CheckPermission
import com.unchil.gismemocompose.shared.composables.LocalPermissionsManager
import com.unchil.gismemocompose.shared.composables.PermissionRequiredCompose
import com.unchil.gismemocompose.shared.composables.PermissionRequiredComposeFuncName
import com.unchil.gismemocompose.shared.composables.PermissionsManager
import com.unchil.gismemocompose.shared.utils.FileManager
import com.unchil.gismemocompose.ui.theme.GISMemoTheme
import com.unchil.gismemocompose.viewmodel.WriteMemoViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale


@SuppressLint("SuspiciousIndentation", "MissingPermission", "UnrememberedMutableState")
@OptIn(
    ExperimentalMaterial3Api::class,
    MapsComposeExperimentalApi::class, ExperimentalComposeUiApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun WriteMemoScreen(navController: NavHostController){


    val permissions = listOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val multiplePermissionsState = rememberMultiplePermissionsState( permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)

    var isGranted   by mutableStateOf(true)
    permissions.forEach { chkPermission ->
        isGranted =  isGranted && multiplePermissionsState.permissions.find { it.permission == chkPermission }?.status?.isGranted
            ?: false
    }

    PermissionRequiredCompose(
        isGranted = isGranted,
        multiplePermissions = permissions ,
        viewType = PermissionRequiredComposeFuncName.MemoMap
    ) {


        val context = LocalContext.current
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

        val repository = LocalRepository.current
        val viewModel = remember {
            WriteMemoViewModel(
                repository = repository)
        }

        val isUsableHaptic = LocalUsableHaptic.current
        val hapticFeedback = LocalHapticFeedback.current
        val coroutineScope = rememberCoroutineScope()
        fun hapticProcessing() {
            if (isUsableHaptic) {
                coroutineScope.launch {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        }

        val fusedLocationProviderClient = remember {
            LocationServices.getFusedLocationProviderClient(context)
        }

        var currentLocation by remember {
            mutableStateOf(LatLng(0.0, 0.0))
        }

        var location: Location? by remember {
            mutableStateOf(null)
        }


        var isGoCurrentLocation by remember { mutableStateOf(false) }
        val isUsableDarkMode = LocalUsableDarkMode.current
        var isDarkMode by remember { mutableStateOf(isUsableDarkMode) }
        var mapTypeIndex by rememberSaveable { mutableStateOf(0) }
        val markerState = MarkerState(position = currentLocation)
        val defaultCameraPosition = CameraPosition.fromLatLngZoom(currentLocation, 16f)
        val cameraPositionState = CameraPositionState(position = defaultCameraPosition)

        var mapProperties by remember {
            mutableStateOf(
                MapProperties(
                    mapType = MapType.entries.first { item ->
                        item.name == MapTypeMenuData.Types[mapTypeIndex].name
                    },
                    isMyLocationEnabled = true,
                    mapStyleOptions = if(isDarkMode) {
                         MapStyleOptions.loadRawResourceStyle(
                            context,
                            R.raw.mapstyle_night
                        )
                    } else { null }
                )
            )
        }

        val uiSettings by remember { mutableStateOf(MapUiSettings(zoomControlsEnabled = false)) }

        val sheetState = SheetState(
            skipPartiallyExpanded = false,
            initialValue = SheetValue.Hidden,
            skipHiddenState = false,
            density = LocalDensity.current
        )

        val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
        var isTagDialog by rememberSaveable { mutableStateOf(false) }
        val isAlertDialog = rememberSaveable { mutableStateOf(false) }
        var isSnapShot by remember { mutableStateOf(false) }
        var isDefaultSnapShot by rememberSaveable { mutableStateOf(false) }
        var isMapClear by remember { mutableStateOf(false) }

        val currentPolyline = remember { mutableStateListOf<LatLng>() }


        var isDrawing by rememberSaveable { mutableStateOf(false) }

        val selectedTagArray: MutableState<ArrayList<Int>> =
            rememberSaveable { mutableStateOf(arrayListOf()) }

        var isLock by rememberSaveable { mutableStateOf(false) }
        var isMark by rememberSaveable { mutableStateOf(false) }


// Not recompose rememberSaveable 에 mutableStatelist 는


        val polylineListR: MutableList<DrawingPolyline> = rememberSaveable { mutableListOf() }
        var polylineList: SnapshotStateList<List<LatLng>>  =  polylineListR.toMutableStateList()

        val snapShotList: MutableList<String> = rememberSaveable { mutableListOf() }

        var alignmentSaveMenuList: Alignment   by remember {  mutableStateOf (Alignment.TopCenter)}
        var alignmentMyLocation: Alignment   by remember {  mutableStateOf ( Alignment.TopStart)}
        var alignmentCreateMenuList: Alignment by remember {  mutableStateOf ( Alignment.BottomStart)}
        var alignmentSettingMenuList: Alignment by remember {  mutableStateOf (Alignment.BottomStart)}
            var alignmentDrawingMenuList: Alignment by remember {  mutableStateOf ( Alignment.BottomEnd)}
        var alignmentMapTypeMenuList: Alignment by remember { mutableStateOf ( Alignment.CenterEnd)}

        val configuration = LocalConfiguration.current


        LaunchedEffect(key1 = configuration.orientation ){

            when (configuration.orientation) {

                Configuration.ORIENTATION_PORTRAIT -> {
                    polylineList = polylineListR.toMutableStateList()
                    alignmentSaveMenuList = Alignment.TopCenter
                    alignmentMyLocation = Alignment.TopStart
                    alignmentCreateMenuList = Alignment.CenterStart
                    alignmentSettingMenuList = Alignment.BottomStart
                    alignmentDrawingMenuList = Alignment.BottomEnd
                    alignmentMapTypeMenuList = Alignment.CenterEnd

                }
                else -> {
                    polylineList = polylineListR.toMutableStateList()
                    alignmentSaveMenuList = Alignment.TopCenter
                    alignmentMyLocation = Alignment.TopStart
                    alignmentCreateMenuList = Alignment.CenterStart
                    alignmentSettingMenuList = Alignment.BottomStart
                    alignmentDrawingMenuList = Alignment.BottomEnd
                    alignmentMapTypeMenuList = Alignment.CenterEnd
                }
            }

        }



        val snackbarHostState = remember { SnackbarHostState() }
        val channel = remember { Channel<Int>(Channel.CONFLATED) }
        LaunchedEffect(channel) {
            channel.receiveAsFlow().collect { index ->
                val channelData = SnackBarChannelObject.entries.first { item ->
                    item.channel == index
                }


                val result = snackbarHostState.showSnackbar(
                    message = context.resources.getString( channelData.message),
                    actionLabel = channelData.actionLabel,
                    withDismissAction = channelData.withDismissAction,
                    duration = channelData.duration
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        hapticProcessing()
                        when (channelData.channelType) {
                            SnackBarChannelObject.Type.MEMO_CLEAR_REQUEST  -> {

                                viewModel.onEvent(WriteMemoViewModel.Event.InitMemo)
                                snapShotList.clear()
                                isLock = false
                                isMark = false
                                isMapClear = true
                                selectedTagArray.value = arrayListOf()

                                channel.trySend(SnackBarChannelObject.entries.first { item ->
                                    item.channelType == SnackBarChannelObject.Type.MEMO_CLEAR_RESULT
                                }.channel)
                            }
                            else -> {

                            }
                        }


                    }
                    SnackbarResult.Dismissed -> {
                        hapticProcessing()
                    }
                }
            }
        }


        LaunchedEffect(key1 = currentLocation) {
            if (currentLocation == LatLng(0.0, 0.0)) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(context.mainExecutor) { task ->
                    if (task.isSuccessful && task.result != null) {
                        location = task.result
                        currentLocation = task.result.toLatLng()
                    }
                }
            }
        }

        var isConnect by   remember { mutableStateOf(context.checkInternetConnected()) }

        LaunchedEffect(key1 = isConnect ){
            while(!isConnect) {
                delay(500)
                isConnect = context.checkInternetConnected()

            }
        }



        val onMapLongClickHandler: (LatLng) -> Unit = {
            hapticProcessing()
            currentLocation = it
        }

        val addPolyline: (MotionEvent) -> Unit = { event ->
            lifecycleOwner.lifecycleScope.launch {
                cameraPositionState.projection?.let {

                    val latLng =
                        it.fromScreenLocation(Point(Math.round(event.x), Math.round(event.y)))

                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            currentPolyline.add(latLng)
                        }
                        MotionEvent.ACTION_MOVE -> {
                            currentPolyline.add(latLng)
                        }
                        MotionEvent.ACTION_UP -> {

                            polylineList.add(currentPolyline.toList())
                            polylineListR.add(currentPolyline.toList())
                            currentPolyline.clear()

                        }
                        else -> {}
                    }
                }
            }
        }


        val deleteSnapshotHandle: ((page: Int) -> Unit) = { page ->
            snapShotList.removeAt(page)
        }


        val isVisibleMenu = rememberSaveable {
            mutableStateOf(false)
        }

        val tagDialogDissmissHandler:() -> Unit = {

                hapticProcessing()
                selectedTagArray.value.clear()
                TagInfoDataObject.entries.forEachIndexed { index, tagInfoData ->
                    if (tagInfoData.isSet.value) {
                        selectedTagArray.value.add(index)
                    }
                }
        }

        val checkEnableLocationService:()-> Unit = {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener(context.mainExecutor) { task ->
                if (!task.isSuccessful || task.result == null) {
                    channel.trySend(SnackBarChannelObject.entries.first {item ->
                        item.channelType == SnackBarChannelObject.Type.LOCATION_SERVICE_DISABLE
                    }.channel)
                }
            }
        }

        val saveHandler: (title: String) -> Unit = { title ->
            val desc = String.format( context.resources.getString(R.string.memo_desc),
                viewModel.snapShotListStateFlow.value.size,
                viewModel.audioTextStateFlow.value.size,
                viewModel.photoListStateFlow.value.size,
                viewModel.videoListStateFlow.value.size
            )

            var snippets = ""
            selectedTagArray.value.forEach {
                 snippets = "${snippets} #${  context.resources.getString( TagInfoDataObject.entries[it].name)   }"
            }


            val id = System.currentTimeMillis()

                viewModel.onEvent(
                    WriteMemoViewModel.Event.UploadMemo(
                        id = id,
                        isLock = isLock,
                        isMark = isMark,
                        selectedTagArrayList = selectedTagArray.value,
                        title = title,
                        desc = desc,
                        snippets = snippets,
                        location = CURRENTLOCATION_TBL(dt=id, latitude = currentLocation.latitude.toFloat(), longitude = currentLocation.longitude.toFloat(), 0f)
                    )
                )


            channel.trySend(SnackBarChannelObject.entries.first { item ->
                item.channelType == SnackBarChannelObject.Type.MEMO_SAVE
            }.channel)


            snapShotList.clear()
            isLock = false
            isMark = false
            isMapClear = true
            selectedTagArray.value = arrayListOf()

        }


        BottomSheetScaffold(
            modifier = Modifier,
            scaffoldState = scaffoldState,
            sheetContent = {
                MemoDataCompose(
                    onEvent = viewModel::onEvent,
                    deleteHandle = deleteSnapshotHandle,
                    channel = channel
                )
            },
            sheetShape = ShapeDefaults.Small,
            sheetPeekHeight = 90.dp,
            sheetDragHandle = {
                Box(
                    modifier = Modifier.height(30.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        modifier = Modifier.scale(1f).clickable {
                            coroutineScope.launch {
                                if (scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden
                                    || scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded
                                ) {
                                    scaffoldState.bottomSheetState.expand()
                                } else {
                                    scaffoldState.bottomSheetState.hide()
                                }
                            }
                        },
                        imageVector = if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded)   Icons.Outlined.KeyboardArrowDown else Icons.Outlined.KeyboardArrowUp,
                        contentDescription = "search",
                    )
                }
            },
            sheetContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
            sheetContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { padding ->


            Box(Modifier.fillMaxSize()) {


                GoogleMap(
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = uiSettings,
                    onMapLongClick = onMapLongClickHandler,
                    onMapClick = {
                        if(isTagDialog) {
                            tagDialogDissmissHandler.invoke()
                            isTagDialog = false
                        }
                    },
                    onMyLocationButtonClick = {
                        checkEnableLocationService.invoke()
                        return@GoogleMap false

                    }
                ) {


                    MapEffect(key1 = isSnapShot, key2 = isMapClear, key3 = isGoCurrentLocation,) { map ->
                        if (isSnapShot) {
                            map.snapshot { bitmap ->
                                val filePath = FileManager.getFilePath(
                                    context,
                                    FileManager.Companion.OUTPUTFILE.IMAGE
                                )
                                bitmap?.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    100,
                                    FileOutputStream(filePath)
                                )
                                snapShotList.add(filePath)
                                viewModel.onEvent(WriteMemoViewModel.Event.SetSnapShot(snapShotList.toList()))
                                isSnapShot = false

                                if (!isDefaultSnapShot) {
                                    channel.trySend(SnackBarChannelObject.entries.first { item ->
                                        item.channelType == SnackBarChannelObject.Type.SNAPSHOT_RESULT
                                    }.channel)
                                }
                            }
                        }

                        if (isMapClear) {
                            map.clear()
                            isMapClear = false
                        }

                        if(isGoCurrentLocation) {
                            map.animateCamera( CameraUpdateFactory.newLatLngZoom( currentLocation, 16F) )
                            isGoCurrentLocation = false
                        }

                    }



                    polylineList.forEach {
                        Polyline(
                            clickable = !isDrawing,
                            points = it,
                            geodesic = true,
                            color = Color.Yellow,
                            width = 20F,
                            onClick = { polyline ->
                                hapticProcessing()
                                polylineList.remove(polyline.points)
                                polylineListR.remove(polyline.points)
                            }

                        )
                    }

                    if (currentPolyline.isNotEmpty()) {
                        Polyline(
                            points = currentPolyline.toList(),
                            geodesic = true,
                            color = Color.Red,
                            width = 5F,
                        )
                    }

                    Marker(
                        state = markerState,
                        title = "lat/lng:(${
                            String.format(
                                "%.5f",
                                markerState.position.latitude
                            )
                        },${String.format("%.5f", markerState.position.longitude)})",
                    )

                }




                if (isDrawing) {
                    Canvas(modifier = Modifier
                        .fillMaxSize()
                        .pointerInteropFilter { event ->
                            addPolyline(event)
                            true
                        }
                    ) {}
                }


                ScaleBar(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 50.dp, end = 10.dp),
                    cameraPositionState = cameraPositionState
                )


                Row(
                    modifier = Modifier
                        .align(alignmentSaveMenuList)
                        .padding(2.dp)
                        .background(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        )
                ) {

                    SaveMenuData.Types.forEach { menuType ->
                        AnimatedVisibility(
                            visible = isVisibleMenu.value,
                        ) {

                            IconButton(
                                onClick = {
                                    hapticProcessing()
                                    when (menuType) {
                                        SaveMenuData.Type.CLEAR -> {
                                            channel.trySend(SnackBarChannelObject.entries.first {item ->
                                                item.channelType == SnackBarChannelObject.Type.MEMO_CLEAR_REQUEST
                                            }.channel)
                                        }
                                        SaveMenuData.Type.SAVE -> {
                                            if (snapShotList.isEmpty()) {
                                                isSnapShot = true
                                                isDefaultSnapShot = true
                                            }
                                            isConnect  = context.checkInternetConnected()

                                            if(isConnect){
                                                viewModel.onEvent(
                                                    WriteMemoViewModel.Event.SearchWeather(
                                                        currentLocation
                                                    )
                                                )
                                                isAlertDialog.value = true
                                            }

                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = SaveMenuData.desc(menuType).first,
                                    contentDescription = menuType.name,
                                )
                            }

                        }
                    }

                }



                Column(
                    modifier = Modifier
                        .align(alignmentMyLocation)
                        .padding(2.dp)
                        .background(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        )
                ) {

                    AnimatedVisibility(
                        visible = isVisibleMenu.value,
                    ) {

                        IconButton(
                            onClick = {
                                hapticProcessing()
                                isGoCurrentLocation = true
                            }
                        ) {
                            Icon(
                                modifier = Modifier.scale(1f),
                                imageVector = Icons.Outlined.ModeOfTravel,
                                contentDescription = "ModeOfTravel",
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = isVisibleMenu.value,
                    ) {

                        IconButton(
                            enabled = if(mapTypeIndex == 0) true else false,
                            onClick = {
                                hapticProcessing()
                                isDarkMode = !isDarkMode

                                if (isDarkMode) {
                                    mapProperties = mapProperties.copy(
                                        mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                                            context,
                                            R.raw.mapstyle_night
                                        )
                                    )
                                } else {
                                    mapProperties = mapProperties.copy(mapStyleOptions = null)
                                }
                            }
                        ) {
                            Icon(
                                modifier = Modifier.scale(1f),
                                imageVector = if (isDarkMode) Icons.Outlined.BedtimeOff else Icons.Outlined.DarkMode,
                                contentDescription = "DarkMode",
                            )
                        }
                    }

                }


                Column(
                    modifier = Modifier
                        .align(alignmentCreateMenuList)
                        .padding(2.dp)
                        .background(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        )
                ) {
                    CreateMenuData.Types.forEach { menuType ->
                        AnimatedVisibility(
                            visible = isVisibleMenu.value,
                        ) {
                            IconButton(
                                onClick = {
                                    hapticProcessing()
                                    when (menuType) {
                                        CreateMenuData.Type.SNAPSHOT -> {
                                            isSnapShot = true
                                        }
                                        CreateMenuData.Type.RECORD -> {
                                            viewModel.onEvent(
                                                WriteMemoViewModel.Event.ToRoute(
                                                    navController, CreateMenuData.desc(menuType).second ?: ""
                                                )
                                            )
                                        }
                                        CreateMenuData.Type.CAMERA -> {
                                            viewModel.onEvent(
                                                WriteMemoViewModel.Event.ToRoute(
                                                    navController, CreateMenuData.desc(menuType).second ?: ""
                                                )
                                            )
                                        }
                                    }
                                }) {
                                Icon(
                                    imageVector = CreateMenuData.desc(menuType).first,
                                    contentDescription = menuType.name,
                                )
                            }
                        }
                    }
                }


                Row(
                    modifier = Modifier
                        .align(alignmentDrawingMenuList)
                        .padding(2.dp)
                        .background(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        )
                ) {


                    DrawingMenuData.Types.forEach { menuType ->
                        AnimatedVisibility(
                            visible = isVisibleMenu.value,
                        ) {

                            IconButton(
                                onClick = {
                                    hapticProcessing()
                                    when (menuType) {

                                        DrawingMenuData.Type.Draw -> {
                                            isDrawing = true
                                            viewModel.onEvent(
                                                WriteMemoViewModel.Event.UpdateIsDrawing(
                                                    isDrawing
                                                )
                                            )

                                        }

                                        DrawingMenuData.Type.Swipe -> {
                                            isDrawing = false
                                            viewModel.onEvent(
                                                WriteMemoViewModel.Event.UpdateIsEraser(
                                                    !isDrawing
                                                )
                                            )
                                        }

                                        DrawingMenuData.Type.Eraser -> {
                                            polylineList.clear()
                                            polylineListR.clear()
                                            isMapClear = true
                                        }


                                    }
                                }) {


                                val iconColor:Color?  = when (menuType) {
                                    DrawingMenuData.Type.Draw -> {
                                        if(isDrawing) DrawingMenuData.desc(menuType).second else null
                                    }
                                    DrawingMenuData.Type.Swipe -> {
                                        if(!isDrawing) DrawingMenuData.desc(menuType).second else null
                                    }
                                    else -> {
                                        null
                                    }

                                }


                                Icon(
                                    imageVector =  DrawingMenuData.desc(menuType).first,
                                    contentDescription = menuType.name,
                                    tint = iconColor?: androidx.compose.material3.MaterialTheme.colorScheme.secondary
                                )
                            }

                        }
                    }

                }


                Row(
                    modifier = Modifier
                        .align(alignmentSettingMenuList)
                        .padding(2.dp)
                        .background(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        )
                ) {

                    IconButton(
                        onClick = {
                            hapticProcessing()
                            isVisibleMenu.value = !isVisibleMenu.value
                        }
                    ) {
                        Icon(
                            modifier = Modifier.scale(1f),
                            imageVector = if (isVisibleMenu.value) Icons.Outlined.OpenWith else Icons.Outlined.Api,
                            contentDescription = "Menu",
                        )
                    }

                    SettingMenuData.Types.forEach { menuType ->
                        AnimatedVisibility(
                            visible = isVisibleMenu.value,
                        ) {
                            IconButton(

                                onClick = {
                                    hapticProcessing()
                                    when (menuType) {
                                        SettingMenuData.Type.SECRET -> {
                                            isLock = !isLock
                                            viewModel.onEvent(
                                                WriteMemoViewModel.Event.UpdateIsLock(
                                                    isLock
                                                )
                                            )
                                        }
                                        SettingMenuData.Type.MARKER -> {
                                            isMark = !isMark
                                            viewModel.onEvent(
                                                WriteMemoViewModel.Event.UpdateIsMarker(
                                                    isMark
                                                )
                                            )
                                        }
                                        SettingMenuData.Type.TAG -> {
                                            isTagDialog = !isTagDialog
                                            if(!isTagDialog) {
                                                tagDialogDissmissHandler.invoke()
                                            }

                                        }
                                    }
                                }) {
                                val icon = when (menuType) {
                                    SettingMenuData.Type.SECRET -> {
                                        if (isLock) SettingMenuData.desc(menuType).first else SettingMenuData.desc(menuType).second
                                            ?: SettingMenuData.desc(menuType).first
                                    }
                                    SettingMenuData.Type.MARKER -> {
                                        if (isMark) SettingMenuData.desc(menuType).first else SettingMenuData.desc(menuType).second
                                            ?: SettingMenuData.desc(menuType).first
                                    }
                                    else -> {
                                        SettingMenuData.desc(menuType).first
                                    }

                                }

                                Icon(
                                    imageVector = icon,
                                    contentDescription = menuType.name,
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                if (scaffoldState.bottomSheetState.currentValue == SheetValue.Hidden
                                    || scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded
                                ) {
                                    scaffoldState.bottomSheetState.expand()
                                } else {
                                    scaffoldState.bottomSheetState.hide()
                                }
                            }
                        },

                        ) {
                        Icon(
                            modifier = Modifier,
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = "Data Container"
                        )
                    }


                }



                Column(
                    modifier = Modifier
                        .align(alignmentMapTypeMenuList)
                        .padding(2.dp)
                        .background(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        )
                ) {
                    //MapTypeMenuList.forEach {
                    MapTypeMenuData.Types.forEachIndexed { index, menuType ->
                        AnimatedVisibility(
                            visible = isVisibleMenu.value,
                        ) {
                            IconButton(
                                onClick = {
                                    hapticProcessing()
                                    val findType = MapType.entries.first { item ->
                                        item.name == menuType.name
                                    }
                                    mapProperties = mapProperties.copy(mapType = findType)
                                    mapTypeIndex = index



                                }) {

                                Icon(
                                    imageVector = MapTypeMenuData.desc(menuType).first,
                                    contentDescription = menuType.name,
                                )
                            }
                        }
                    }
                }


                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .background(
                                color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                                shape = ShapeDefaults.ExtraSmall)
                            .align(Alignment.Center)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {

                        AnimatedVisibility(visible = isTagDialog) {

                            AssistChipGroupCompose(
                                isVisible = isTagDialog,
                                setState = selectedTagArray,
                            ) {

                            }

                        }

                    }



                if (isAlertDialog.value) {
                    ConfirmDialog(
                        isAlertDialog = isAlertDialog,
                        cancelSnapShot = {
                            if(isDefaultSnapShot) {
                                viewModel.onEvent(
                                    WriteMemoViewModel.Event.DeleteMemoItem( WriteMemoData.Type.SNAPSHOT, 0)
                                )
                                snapShotList.clear()
                                isDefaultSnapShot = false
                            }
                        },
                        onEvent = saveHandler
                    )
                }

                if (!isConnect) {
                    ChkNetWork(
                        onCheckState = {
                            coroutineScope.launch {
                                isConnect =  context.checkInternetConnected()
                            }
                        }
                    )
                }



            }// Box

        }// BottomSheetScaffold

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDialog(
    isAlertDialog: MutableState<Boolean> ,
    cancelSnapShot:()->Unit,
    onEvent: (title:String) -> Unit,
) {

    val context = LocalContext.current
    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()




    val titleTimeStamp = SimpleDateFormat(
        "yyyy-MM-dd HH:mm",
        Locale.getDefault()
    ).format(System.currentTimeMillis())

    val titleText = rememberSaveable {
        mutableStateOf(titleTimeStamp)
    }


    val recognizerIntent = remember { recognizerIntent }

    val startLauncherRecognizerIntent = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        if (it.resultCode == Activity.RESULT_OK) {

            val result =
                it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            titleText.value = titleText.value + result?.get(0).toString() + " "

        }
    }


    BasicAlertDialog(
        onDismissRequest = {
            isAlertDialog.value = false
            cancelSnapShot.invoke()
        }
    ) {

            Column(
                modifier = Modifier
                    .background(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                    shape = ShapeDefaults.ExtraSmall)
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    modifier = Modifier.padding(top = 20.dp),
                    text = context.resources.getString(R.string.writeMemo_AlertDialog_Title),
                    textAlign = TextAlign.Center,
                    style =  androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth(),
                    singleLine = false,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            IconButton(
                                modifier = Modifier,
                                onClick = {
                                    hapticProcessing(coroutineScope, hapticFeedback, isUsableHaptic)
                                    titleText.value = ""
                                    startLauncherRecognizerIntent.launch(recognizerIntent())
                                },
                                content = {
                                    Icon(
                                        modifier = Modifier,
                                        imageVector = Icons.Outlined.Mic,
                                        contentDescription = "SpeechToText"
                                    )
                                }
                            )



                            IconButton(
                                onClick = {
                                    hapticProcessing(coroutineScope, hapticFeedback, isUsableHaptic)
                                    titleText.value = ""
                                }) {
                                Icon(
                                    imageVector = Icons.Rounded.Replay,
                                    contentDescription = "Clear"
                                )
                            }

                        }
                    },
                    value = titleText.value,
                    onValueChange = { titleText.value = it },
                    label = { androidx.compose.material3.Text(context.resources.getString(R.string.writeMemo_AlertDialog_title_label)) },
                    shape = OutlinedTextFieldDefaults.shape,
                    keyboardActions = KeyboardActions.Default
                )

                Text(
                    text = context.resources.getString(R.string.writeMemo_AlertDialog_desc),
                    style =  androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {


                    TextButton(

                        onClick = {
                            hapticProcessing(coroutineScope, hapticFeedback, isUsableHaptic)
                            isAlertDialog.value = false
                            cancelSnapShot.invoke()
                        }
                    ) {
                        Text(
                            context.resources.getString(R.string.writeMemo_AlertDialog_Cancel),
                            textAlign = TextAlign.Center,
                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    }


                    TextButton(

                        onClick = {
                            hapticProcessing(coroutineScope, hapticFeedback, isUsableHaptic)
                            isAlertDialog.value = false
                            onEvent(titleText.value)
                        }
                    ) {
                        Text(
                            context.resources.getString(R.string.writeMemo_AlertDialog_Confirm),
                            textAlign = TextAlign.Center,
                            style  = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    }


                }
            }

    }



}






@Preview
@Composable
fun PrevWriteMemo(){
    val permissionsManager = PermissionsManager()
    val navController = rememberNavController()

    CompositionLocalProvider(LocalPermissionsManager provides permissionsManager) {


            GISMemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.onPrimary,
                    contentColor = MaterialTheme.colors.primary
                ) {
                    WriteMemoScreen(navController = navController)
                }
            }

    }


}



