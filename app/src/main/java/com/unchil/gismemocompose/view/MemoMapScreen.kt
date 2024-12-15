package com.unchil.gismemocompose.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.res.Configuration
import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Api
import androidx.compose.material.icons.outlined.BedtimeOff
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.ModeOfTravel
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil3.size.Size
import coil3.toUri
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
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.widgets.ScaleBar
import com.unchil.gismemocompose.LocalUsableDarkMode
import com.unchil.gismemocompose.LocalUsableHaptic
import com.unchil.gismemocompose.R
import com.unchil.gismemocompose.data.LocalRepository
import com.unchil.gismemocompose.model.MapTypeMenuData
import com.unchil.gismemocompose.model.SnackBarChannelObject
import com.unchil.gismemocompose.shared.composables.CheckPermission
import com.unchil.gismemocompose.shared.composables.PermissionRequiredCompose
import com.unchil.gismemocompose.shared.composables.PermissionRequiredComposeFuncName
import com.unchil.gismemocompose.viewmodel.MemoMapViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import com.unchil.gismemocompose.db.entity.MEMO_TBL
import com.unchil.gismemocompose.model.BiometricCheckObject
import com.unchil.gismemocompose.navigation.GisMemoDestinations
import com.unchil.gismemocompose.shared.SwipeBoxHeight
import com.unchil.gismemocompose.shared.biometricPrompt


@SuppressLint("UnrememberedMutableState", "MissingPermission")
@OptIn(
    ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class,
    MapsComposeExperimentalApi::class
)
@Composable
fun MemoMapScreen(navController: NavHostController){
    val permissions = listOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val multiplePermissionsState = rememberMultiplePermissionsState( permissions)
    CheckPermission(multiplePermissionsState = multiplePermissionsState)

    var isGranted by mutableStateOf(true)
    permissions.forEach { chkPermission ->
        isGranted =  isGranted && multiplePermissionsState.permissions.find {
            it.permission == chkPermission
        }?.status?.isGranted ?: false
    }

    PermissionRequiredCompose(
        isGranted = isGranted,
        multiplePermissions = permissions ,
        viewType = PermissionRequiredComposeFuncName.MemoMap
    ) {

        val context = LocalContext.current
        val isUsableHaptic = LocalUsableHaptic.current
        val hapticFeedback = LocalHapticFeedback.current
        val coroutineScope = rememberCoroutineScope()

        val repository = LocalRepository.current
        val viewModel = remember { MemoMapViewModel( repository = repository ) }

        val fusedLocationProviderClient = remember {
            LocationServices.getFusedLocationProviderClient(context)
        }
        var currentLocation by remember {
            mutableStateOf(LatLng(0.0,0.0))
        }
        var location: Location? by remember {
            mutableStateOf(null)
        }
        var isGoCurrentLocation by remember { mutableStateOf(false) }

        val isUsableDarkMode = LocalUsableDarkMode.current
        var isDarkMode by remember { mutableStateOf(isUsableDarkMode) }
        var mapTypeIndex by rememberSaveable { mutableStateOf(0) }

        LaunchedEffect( key1 =  currentLocation){
            if( currentLocation == LatLng(0.0,0.0)) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(
                    context.mainExecutor
                ) { task ->
                    if (task.isSuccessful && task.result != null ) {
                        location = task.result
                        currentLocation = task.result.toLatLng()
                    }
                }
                viewModel.onEvent(MemoMapViewModel.Event.SetMarkers)
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
                        hapticProcessing(coroutineScope , hapticFeedback, isUsableHaptic)
                        when (channelData.channelType) {
                            else -> {  }
                        }
                    }
                    SnackbarResult.Dismissed -> {
                        hapticProcessing(coroutineScope , hapticFeedback, isUsableHaptic)
                    }
                }
            }
        }

        val checkEnableLocationService:()-> Unit = {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener(
                context.mainExecutor
            ) { task ->
                if (!task.isSuccessful || task.result == null) {
                    channel.trySend(SnackBarChannelObject.entries.first {item ->
                        item.channelType == SnackBarChannelObject.Type.LOCATION_SERVICE_DISABLE
                    }.channel)
                }
            }
        }

        val markerMemoList = viewModel.markerMemoList.collectAsState()

        // No ~~~~ remember
        val markerState =  MarkerState( position = currentLocation )
        val defaultCameraPosition =  CameraPosition.fromLatLngZoom( currentLocation, 16f)
        val cameraPositionState =  CameraPositionState(position = defaultCameraPosition)

        var mapProperties by remember {
            mutableStateOf(
                MapProperties(
                    mapType = MapType.entries.first { mapType ->
                        mapType.name == MapTypeMenuData.Types[mapTypeIndex].name },
                    isMyLocationEnabled = true,
                    mapStyleOptions = if(isDarkMode) {
                        MapStyleOptions.loadRawResourceStyle( context, R.raw.mapstyle_night )
                    } else { null }
                )
            )
        }

        val uiSettings by remember {
            mutableStateOf( MapUiSettings( zoomControlsEnabled = false ) )
        }

        val isVisibleMenu = rememberSaveable {
            mutableStateOf(false)
        }

        val sheetState = SheetState(
            skipPartiallyExpanded = false,
            initialValue = SheetValue.Hidden,
            density = Density(context)
        )

        val scaffoldState =  rememberBottomSheetScaffoldState( bottomSheetState = sheetState )

        val onMapLongClickHandler: (LatLng) -> Unit = {
            hapticProcessing(coroutineScope , hapticFeedback, isUsableHaptic)
            currentLocation = it
        }

        val configuration = LocalConfiguration.current

        val cardViewRate:Float =
            when (configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> 1f
                else -> 0.6f
            }

        val isMemoCardView = remember{ mutableStateOf(false) }
        val isCurrentMemo = remember{ mutableStateOf(0L) }

        val topMenu:@Composable () -> Unit = {
            Row(modifier = Modifier) {

            }
        }


        val MapButton: @Composable (String, ()->Unit) -> Unit = { text, onClick ->
            Button(
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.outline,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                onClick = onClick
            ) {
                Text(text = text, style = MaterialTheme.typography.labelSmall)
            }
        }

        val MapTypeControls: @Composable ((MapType) -> Unit) -> Unit = { onClickHandler ->
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(state = ScrollState(0)),
                horizontalArrangement = Arrangement.Center
            ) {
                MapType.entries.filter {
                    it.name == "NORMAL" || it.name == "TERRAIN" || it.name == "HYBRID"
                }.forEach {
                    MapButton(it.toString()){
                        onClickHandler(it)
                    }
                }
            }
        }

        BottomSheetScaffold(
            modifier = Modifier,
            topBar = topMenu,
            scaffoldState = scaffoldState,
            sheetContent = {
                MapTypeControls{
                    mapProperties = mapProperties.copy(mapType = it)
                }
            },
            sheetContainerColor = Color.LightGray.copy(alpha = 0.5f),
            sheetPeekHeight = 0.dp,
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
                    onMyLocationButtonClick = {
                        checkEnableLocationService.invoke()
                        return@GoogleMap false
                    }
                ) {


                    MapEffect(key1 = isGoCurrentLocation){
                        if(isGoCurrentLocation) {
                            it.animateCamera(
                                CameraUpdateFactory.newLatLngZoom( currentLocation, 16F)
                            )
                            isGoCurrentLocation = false
                        }
                    }

                    Marker(
                        state = markerState,
                        title = "lat/lng:(" +
                                "${String.format("%.5f", markerState.position.latitude)}," +
                                "${String.format("%.5f", markerState.position.longitude)})"
                    )

                    markerMemoList.value.forEach {

                        val state = MarkerState(
                            position = LatLng(it.latitude.toDouble(), it.longitude.toDouble()) )


                        MarkerInfoWindowContent(
                            state = state,
                            title = it.title,
                            //   snippet = "${it.snippets}\n${it.desc}",
                            //    onClick = markerClick,
                            //  icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                            draggable = true,
                            onInfoWindowClick = {
                                hapticProcessing(coroutineScope , hapticFeedback, isUsableHaptic)
                                isMemoCardView.value = false
                            },
                            onInfoWindowClose = {},
                            onInfoWindowLongClick = { marker ->
                                hapticProcessing(coroutineScope , hapticFeedback, isUsableHaptic)
                                isMemoCardView.value = true
                                isCurrentMemo.value = it.id
                            },
                        ) { marker ->

                            Column {
                                Text(marker.title ?: "", color = Color.Red)
                            }
                        }

                    }

                }

                if (isMemoCardView.value) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(cardViewRate)
                            .align(Alignment.BottomCenter)
                            .padding(10.dp)
                            .shadow(AppBarDefaults.TopAppBarElevation)
                    ) {

                        MemoView(
                            item = markerMemoList.value.first {
                                it.id == isCurrentMemo.value
                            },
                            viewModel::onEvent,
                            navController = navController
                        )
                    }
                }


                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        )
                ) {

                    AnimatedVisibility(
                        visible = isVisibleMenu.value,
                    ) {

                        IconButton(
                            onClick = {
                                hapticProcessing(coroutineScope , hapticFeedback, isUsableHaptic)
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
                                hapticProcessing(coroutineScope , hapticFeedback, isUsableHaptic)
                                isDarkMode = !isDarkMode

                                mapProperties = if (isDarkMode) {
                                    mapProperties.copy(
                                        mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                                            context,
                                            R.raw.mapstyle_night
                                        )
                                    )
                                } else {
                                    mapProperties.copy(mapStyleOptions = null)
                                }
                            }
                        ) {
                            Icon(
                                modifier = Modifier.scale(1f),
                                imageVector = if (isDarkMode)
                                    Icons.Outlined.BedtimeOff else Icons.Outlined.DarkMode,
                                contentDescription = "DarkMode",
                            )
                        }
                    }

                }




                ScaleBar(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 50.dp, end = 10.dp),
                    cameraPositionState = cameraPositionState
                )


                IconButton(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(2.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        ),
                    onClick = {
                        hapticProcessing(coroutineScope , hapticFeedback, isUsableHaptic)
                        isVisibleMenu.value = !isVisibleMenu.value
                    }
                ) {
                    Icon(
                        modifier = Modifier.scale(1f),
                        imageVector = if (isVisibleMenu.value) Icons.Outlined.OpenWith else Icons.Outlined.Api,
                        contentDescription = "OpenWith",
                    )
                }


                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(2.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
                            shape = ShapeDefaults.ExtraSmall
                        )

                ) {

                    MapTypeMenuData.Types.forEachIndexed { index, it ->
                        AnimatedVisibility(
                            visible = isVisibleMenu.value,
                        ) {
                            IconButton(onClick = {
                                hapticProcessing(coroutineScope , hapticFeedback, isUsableHaptic)
                                val mapType = MapType.entries.first { mapType ->
                                    mapType.name == it.name
                                }
                                mapProperties = mapProperties.copy(mapType = mapType)
                                mapTypeIndex = index

                            }) {
                                Icon(
                                    imageVector = MapTypeMenuData.desc(it).first,
                                    contentDescription = it.name,
                                )
                            }
                        }
                    }


                }

            }
        }





    }
}



@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MemoView(
    item: MEMO_TBL,
    event: ((MemoMapViewModel.Event)->Unit)? = null,
    navController: NavHostController? = null){

    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val permissions = listOf(
        Manifest.permission.USE_BIOMETRIC,
    )
    val multiplePermissionsState = rememberMultiplePermissionsState( permissions)


    var isGranted by mutableStateOf(true)
    permissions.forEach { chkPermission ->
        isGranted = isGranted && multiplePermissionsState.permissions.find {
            it.permission == chkPermission
        }?.status?.isGranted?: false
    }


    fun checkBiometricSupport(): Boolean {
        val isDeviceSecure = ContextCompat.getSystemService(
            context, KeyguardManager::class.java
        )?.isDeviceSecure ?: false
        return isGranted || isDeviceSecure
    }



    val onResult:(
        isSucceeded:Boolean,
        bioMetricCheckType: BiometricCheckObject.Type,
        errorMsg:String? ) -> Unit = { result, bioMetricCheckType, msg ->
        if(result){
            when(bioMetricCheckType){
                BiometricCheckObject.Type.DETAILVIEW -> {
                    navController?.let {navHostController ->
                        event?.let {
                            it(
                                MemoMapViewModel.Event.ToRoute(
                                    navController = navHostController,
                                    route = GisMemoDestinations.DetailMemo.createRoute( id= item.id )
                                )
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }



    Card(
        modifier = Modifier
            .height(260.dp)
            .padding(top = 2.dp),
        shape = ShapeDefaults.ExtraSmall ,
        onClick = {
            hapticProcessing(coroutineScope , hapticFeedback, isUsableHaptic)
            if(item.isSecret && checkBiometricSupport()) {
                biometricPrompt(context, BiometricCheckObject.Type.DETAILVIEW, onResult)
            }else {
                navController?.let {
                    if (event != null) {
                        event(
                            MemoMapViewModel.Event.ToRoute(
                                navController = it,
                                route = GisMemoDestinations.DetailMemo.createRoute( id= item.id )
                            )
                        )
                    }
                }
            }


        }

    ) {

        Box(

            modifier = Modifier
                .fillMaxWidth()
                .height(SwipeBoxHeight)
                .background(MaterialTheme.colorScheme.inverseOnSurface),

            contentAlignment = Alignment.Center
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {

                Icon(
                    modifier = Modifier
                        .scale(1f),
                    imageVector = if (item.isSecret) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                    contentDescription = "Lock",
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text =  item.title,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        minLines =  1,
                    )
                    Text(
                        text =   item.desc,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        minLines =  1,
                    )
                    Text(
                        text =   item.snippets,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        minLines =  1,
                    )
                }

                Icon(
                    modifier = Modifier
                        .scale(1f),
                    imageVector = if (item.isPin) Icons.Outlined.LocationOn else Icons.Outlined.LocationOff,
                    contentDescription = "Mark",
                )


            }
        }

        ImageViewer(data = item.snapshot.toUri() , size = Size.ORIGINAL, isZoomable = false)
    }
}
