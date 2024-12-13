package com.unchil.gismemocompose.view

import android.Manifest
import android.content.res.Configuration
import androidx.biometric.BiometricManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Publish
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.size.Size
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.unchil.gismemocompose.LocalUsableHaptic
import com.unchil.gismemocompose.data.LocalRepository
import com.unchil.gismemocompose.db.entity.MEMO_TBL
import com.unchil.gismemocompose.model.BiometricCheckType
import com.unchil.gismemocompose.model.ListItemBackgroundAction
import com.unchil.gismemocompose.model.SnackBarChannelObject
import com.unchil.gismemocompose.model.getDesc
import com.unchil.gismemocompose.navigation.GisMemoDestinations
import com.unchil.gismemocompose.shared.composables.biometricPrompt
import com.unchil.gismemocompose.shared.launchIntent_Biometric_Enroll
import com.unchil.gismemocompose.shared.launchIntent_ShareMemo
import com.unchil.gismemocompose.ui.theme.GISMemoTheme
import com.unchil.gismemocompose.viewmodel.ListViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

val SwipeBoxHeight = 70.dp

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun IntroView(
    navController: NavHostController
) {

    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val repository = LocalRepository.current
    val coroutineScope = rememberCoroutineScope()

    val viewModel = remember {
        ListViewModel(repository = repository  )
    }


    val result = viewModel.memoListPaging.collectAsLazyPagingItems()
    val isRefreshing = viewModel.isRefreshing.collectAsState()
    val isSearchRefreshing: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val sheetState = SheetState(
        skipPartiallyExpanded = false,
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true,
        density = LocalDensity.current
    )

    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val snackBarHostState = remember { SnackbarHostState() }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing.value,
        onRefresh = {
            viewModel.eventHandler(
                ListViewModel.Event.Search(queryDataList = mutableListOf())
            )
            isSearchRefreshing.value = true
        }
    )

    val channel = remember { Channel<Int>(Channel.CONFLATED) }

    val lazyListState = rememberLazyListState()

    var isPortrait by remember { mutableStateOf(false) }

    var gridWidth by remember { mutableStateOf(1f) }

    var upButtonPaddingValue  by remember {  mutableStateOf ( 10.dp ) }
    var sheetPeekHeightValue  by remember { mutableStateOf ( 0.dp ) }
    var drawerSheetWidthValue  by remember { mutableStateOf ( 0.5f) }
    var listBottomPaddingValue by remember { mutableStateOf (  2.dp) }

    LaunchedEffect(key1 = configuration.orientation ){
        when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                isPortrait = true
                gridWidth = 1f
                upButtonPaddingValue = 160.dp
                sheetPeekHeightValue = 110.dp
                drawerSheetWidthValue = 0f
                listBottomPaddingValue = 110.dp

                if (drawerState.isOpen) {
                        drawerState.close()
                }

            }
            else -> {
                isPortrait = false
                gridWidth = 0.8f
                upButtonPaddingValue = 10.dp
                sheetPeekHeightValue = 0.dp
                drawerSheetWidthValue = 0.5f
                listBottomPaddingValue = 2.dp
            }
        }
    }


    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current

    fun hapticProcessing(){
        if(isUsableHaptic){
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }


    LaunchedEffect(channel) {
        channel.receiveAsFlow().collect { index ->
            val channelInfo = SnackBarChannelObject.entries.first {item ->
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
                    hapticProcessing()
                    //----------
                    when (channelInfo.channelType) {
                        else -> {}
                    }
                    //----------
                }
                SnackbarResult.Dismissed -> {
                    hapticProcessing()
                }
            }
        }
    }

    LaunchedEffect(key1 = result.loadState.source.refresh,) {
        if (!isRefreshing.value) {
            when (result.loadState.source.refresh) {
                is LoadState.Error -> {}
                LoadState.Loading -> {}
                is LoadState.NotLoading -> {

                    if (isSearchRefreshing.value) {

                        channel.trySend(SnackBarChannelObject.entries.first {item ->
                            item.channelType == SnackBarChannelObject.Type.SEARCH_RESULT
                        }.channel)

                        isSearchRefreshing.value = false
                    }

                }
            }
        }
    }


    val searchView: (@Composable () -> Unit) = {
        val sheetControl: (() -> Unit)? =  if (isPortrait) {
            null
        } else {
            { if (drawerState.isOpen) { coroutineScope.launch {  drawerState.close() }  } }
        }
        SearchView(
            isSearchRefreshing = isSearchRefreshing,
            sheetControl = sheetControl,
            onEvent = viewModel.eventHandler
        ) {

            channel.trySend(SnackBarChannelObject.entries.first {item ->
                item.channelType == SnackBarChannelObject.Type.SEARCH_CLEAR
            }.channel)

        }
    }



    ModalNavigationDrawer(
        modifier = Modifier,
        drawerState = drawerState,
        drawerContent = {
            if (!isPortrait) {
                ModalDrawerSheet(
                    modifier = Modifier.fillMaxWidth(drawerSheetWidthValue),
                    drawerShape = ShapeDefaults.ExtraSmall,
                    drawerTonalElevation = 2.dp
                ) {
                    searchView()
                }
            }
        },
        content = {
            BottomSheetScaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                scaffoldState = scaffoldState,
                snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
                sheetPeekHeight = sheetPeekHeightValue,
                sheetShape = ShapeDefaults.Small,
                sheetDragHandle = {
                    Box(
                        modifier = Modifier.height(30.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            modifier = Modifier
                                .scale(1f)
                                .clickable {
                                    coroutineScope.launch {
                                        if (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded) {
                                            scaffoldState.bottomSheetState.expand()
                                        } else {
                                            scaffoldState.bottomSheetState.partialExpand()
                                        }
                                    }
                                },
                            imageVector = if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded)   Icons.Outlined.KeyboardArrowDown else Icons.Outlined.KeyboardArrowUp,
                            contentDescription = "search",
                        )
                    }
                },
                sheetContent = {
                    if (isPortrait) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            searchView()
                        }
                    }
                }
            ) { innerPadding ->

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        if (!isPortrait) {
                            Box(modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .padding(vertical = 2.dp)) {
                                WeatherContent{
                                    if(!it){
                                        channel.trySend(SnackBarChannelObject.entries.first {item ->
                                            item.channelType == SnackBarChannelObject.Type.LOCATION_SERVICE_DISABLE
                                        }.channel)


                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pullRefresh(state = pullRefreshState)

                        ) {



                            LazyColumn(
                                modifier = Modifier.padding(bottom = listBottomPaddingValue),
                                state = lazyListState,
                                userScrollEnabled = true,
                                verticalArrangement = Arrangement.SpaceBetween,
                                contentPadding = PaddingValues(
                                    horizontal = 2.dp,
                                    vertical = 2.dp
                                )
                            ) {

                                if (isPortrait) {
                                    stickyHeader {
                                        WeatherContent{
                                            if(!it){

                                                channel.trySend(SnackBarChannelObject.entries.first {item ->
                                                    item.channelType == SnackBarChannelObject.Type.LOCATION_SERVICE_DISABLE
                                                }.channel)

                                            }
                                        }
                                    }
                                }

                                items(result.itemCount) {
                                    result[it]?.let { memo ->
                                        MemoSwipeView(
                                            item = memo,
                                            channel = channel,
                                            event = viewModel::onEvent,
                                            navController = navController
                                        )
                                    }
                                }

                            }

                            PullRefreshIndicator(
                                refreshing = isRefreshing.value,
                                state = pullRefreshState,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )

                            SearchingProgressIndicator(isVisibility = isSearchRefreshing.value)

                            UpButton(
                                modifier = Modifier
                                    .padding(end = 10.dp, bottom = upButtonPaddingValue)
                                    .align(Alignment.BottomEnd),
                                listState = lazyListState
                            )

                        }

                    }
                }
            }
        } // content
    )


}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
    ExperimentalMaterialApi::class,)
@Composable
fun MemoSwipeView(
    item: MEMO_TBL,
    channel:  Channel<Int>? = null,
    event: ((ListViewModel.Event)->Unit)? = null,
    navController: NavController? = null
){

    val context = LocalContext.current
    val repository = LocalRepository.current


    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    fun hapticProcessing(){
        if(isUsableHaptic){
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }


    val permissions = listOf(
        Manifest.permission.USE_BIOMETRIC,
    )
    val multiplePermissionsState = rememberMultiplePermissionsState( permissions)


    var isGranted by remember { mutableStateOf(true) }
    permissions.forEach { chkPermission ->
        isGranted =  isGranted && multiplePermissionsState.permissions.find { it.permission == chkPermission }?.status?.isGranted
            ?: false
    }


    val onResult: (isSucceeded:Boolean, bioMetricCheckType: BiometricCheckType, errorMsg:String? ) -> Unit =
        { result, bioMetricCheckType, msg ->
            if(result){
                when(bioMetricCheckType){
                    BiometricCheckType.DETAILVIEW -> {
                        navController?.let {
                            if (event != null) {
                                event(
                                    ListViewModel.Event.ToRoute(
                                        navController = it,
                                        route = GisMemoDestinations.DetailMemoView.createRoute(id = item.id.toString())
                                    )
                                )
                            }
                        }
                    }
                    BiometricCheckType.SHARE -> {
                        coroutineScope.launch {
                            launchIntent_ShareMemo(context = context, memo = item, db = repository.database )
                        }
                    }
                    BiometricCheckType.DELETE -> {
                        if (event != null) {
                            event(  ListViewModel.Event.DeleteItem(id = item.id) )
                        }
                        channel?.let {channel ->

                            channel.trySend(SnackBarChannelObject.entries.first {item ->
                                item.channelType == SnackBarChannelObject.Type.MEMO_DELETE
                            }.channel)

                        }
                    }
                }
            }else {
                channel?.let {channel ->
                    channel.trySend(SnackBarChannelObject.entries.first {item ->
                        item.channelType == SnackBarChannelObject.Type.AUTHENTICATION_FAILED
                    }.channel)

                }

            }
        }




    val ANCHOR_OFFSET = 80
    val isAnchor = remember { mutableStateOf(false) }
    val isToStart = remember { mutableStateOf(false) }

    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.Default -> {
                    isAnchor.value = false
                    false
                }
                DismissValue.DismissedToEnd -> {
                    isToStart.value = false
                    isAnchor.value = !isAnchor.value
                    false
                }
                DismissValue.DismissedToStart -> {
                    isToStart.value = true
                    isAnchor.value = !isAnchor.value
                    false
                }
            }
        }
    )

    val dismissContentOffset by  remember {
        mutableStateOf(
            if (isAnchor.value) {
                if (isToStart.value) -ANCHOR_OFFSET.dp else ANCHOR_OFFSET.dp
            } else {
                0.dp
            }
        )
    }

    val checkBiometricSupport: (() -> Unit) = {
        val biometricManager = BiometricManager.from(context)
        when (  biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
                    or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        ) {
            BiometricManager.BIOMETRIC_SUCCESS -> { }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                launchIntent_Biometric_Enroll(context)
            }
            else -> {

                channel?.trySend(SnackBarChannelObject.entries.first {item ->
                    item.channelType == SnackBarChannelObject.Type.BIOMETRIC_NO_SUCCESS
                }.channel)

            }
        }

    }

    Card(
        modifier = Modifier
            .height(260.dp)
            .padding(top = 2.dp) ,
        shape = ShapeDefaults.ExtraSmall ,
        onClick = {
            hapticProcessing()
            if(item.isSecret && isGranted ) {
                checkBiometricSupport.invoke()
                biometricPrompt(context, BiometricCheckType.DETAILVIEW, onResult)
            }else {
                navController?.let {
                    if (event != null) {
                        event(
                            ListViewModel.Event.ToRoute(
                                navController = it,
                                route = GisMemoDestinations.DetailMemoView.createRoute(id = item.id.toString())
                            )
                        )
                    }
                }
            }

        }

    ) {

        SwipeToDismiss(
            modifier = Modifier
            ,
            state = dismissState,
            background = {
                BackgroundContent(dismissState) {
                    when(it){
                        ListItemBackgroundAction.SHARE -> {

                            if(item.isSecret && isGranted ) {
                                checkBiometricSupport.invoke()
                                biometricPrompt(context, BiometricCheckType.SHARE, onResult)
                            }else {
                                coroutineScope.launch {
                                    launchIntent_ShareMemo(context = context, memo = item, db = repository.database )
                                }
                            }

                        }
                        ListItemBackgroundAction.DELETE -> {

                            if(item.isSecret && isGranted ) {
                                checkBiometricSupport.invoke()
                                biometricPrompt(context, BiometricCheckType.DELETE, onResult)
                            }else {
                                if (event != null) {
                                    event(  ListViewModel.Event.DeleteItem(id = item.id) )
                                }
                                channel?.let {channel ->
                                    channel.trySend(SnackBarChannelObject.entries.first {item ->
                                        item.channelType == SnackBarChannelObject.Type.MEMO_DELETE
                                    }.channel)
                                }
                            }
                        }
                    }

                    isAnchor.value = false
                }
            },
            dismissContent = {

                Box(

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SwipeBoxHeight)
                        .offset(x = dismissContentOffset)
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                    ,

                    contentAlignment = Alignment.Center
                ) {

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable(false, null, null) {}
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {


                        Icon(
                            modifier = Modifier.scale(1f),
                            imageVector = if (item.isSecret) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                            contentDescription = "Lock",
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.8f),
                            verticalArrangement = Arrangement.SpaceAround,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                               text =  item.title,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                minLines =  1,
                                style = androidx.compose.material3.MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text =   item.desc,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                minLines =  1,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text =   item.snippets,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                minLines =  1,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                            )
                        }

                        Icon(
                            modifier = Modifier.scale(1f),
                            imageVector = if (item.isPin) Icons.Outlined.LocationOn else Icons.Outlined.LocationOff,
                            contentDescription = "Mark",
                        )


                    }
                }

            }
        )

        ImageViewer(data = item.snapshot.toUri() , size = Size.ORIGINAL, isZoomable = false)

    }

}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun BackgroundContent(
    dismissState: DismissState,
    onClick:(ListItemBackgroundAction)->Unit
){

    val color by animateColorAsState(
        when (dismissState.targetValue) {
            DismissValue.Default -> androidx.compose.material3.MaterialTheme.colorScheme.surface
            DismissValue.DismissedToEnd -> Color.Blue.copy(alpha = 0.3f)
            DismissValue.DismissedToStart -> Color.Red.copy(alpha = 0.3f)
        }
    )
    val scale by animateFloatAsState(
        when (dismissState.targetValue == DismissValue.Default) {
            true -> 1f
            else -> 1.3f
        }
    )



    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    fun hapticProcessing(){
        if(isUsableHaptic){
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }



    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SwipeBoxHeight)
            .background(color)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Box(
            modifier = Modifier,
            contentAlignment = Alignment.CenterStart
        ) {

            Row {

                IconButton(
                    onClick = {
                        hapticProcessing()
                        onClick(ListItemBackgroundAction.SHARE)
                    }
                ) {
                    Icon(
                        modifier = Modifier.scale(scale),
                        imageVector = ListItemBackgroundAction.SHARE.getDesc().second,
                        contentDescription = ListItemBackgroundAction.SHARE.getDesc().first
                    )
                }
            }
        }


        Box(
            modifier = Modifier,
            contentAlignment = Alignment.CenterEnd
        ) {

            Row {
                IconButton(
                    onClick = {
                        hapticProcessing()
                        //  isAnchor.value = false
                        onClick(ListItemBackgroundAction.DELETE)
                    }
                ) {
                    Icon(
                        modifier = Modifier.scale(scale),
                        imageVector = ListItemBackgroundAction.DELETE.getDesc().second,
                        contentDescription = ListItemBackgroundAction.DELETE.getDesc().first
                    )
                }
            }
        }

    }

}


@Composable
fun SearchingProgressIndicator(
    isVisibility:Boolean
){
    if(isVisibility) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


@Composable
fun UpButton(
    modifier:Modifier,
    listState: LazyListState
){


    val showButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }


    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    fun hapticProcessing(){
        if(isUsableHaptic){
            coroutineScope.launch {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    if( showButton) {
        FloatingActionButton(
            modifier = Modifier.then(modifier),
            elevation =  FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
            onClick = {
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                    hapticProcessing()
                }
            }
        ) {
            Icon(
                modifier = Modifier.scale(1f),
                imageVector = Icons.Outlined.Publish,
                contentDescription = "Up",

            )

        }
    }
}



@Preview
@Composable
fun PrevIntroView(){

    val navController = rememberNavController()

    GISMemoTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.onPrimary,
            contentColor = MaterialTheme.colors.primary
        ) {
            IntroView(navController)
        }
    }
}