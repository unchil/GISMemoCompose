package com.unchil.gismemocompose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Publish
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.unchil.gismemocompose.LocalUsableHaptic
import com.unchil.gismemocompose.data.LocalRepository
import com.unchil.gismemocompose.data.RepositoryProvider
import com.unchil.gismemocompose.db.LuckMemoDB
import com.unchil.gismemocompose.model.SearchQueryData
import com.unchil.gismemocompose.model.SnackBarChannelObject
import com.unchil.gismemocompose.shared.composables.LocalPermissionsManager
import com.unchil.gismemocompose.shared.composables.PermissionsManager
import com.unchil.gismemocompose.ui.theme.GISMemoTheme
import com.unchil.gismemocompose.viewmodel.MemoListViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MemoListCompose(
    navController: NavHostController,
    viewModel: MemoListViewModel,
    scaffoldState: BackdropScaffoldState,
    channel:  Channel<Int>? = null,
){

    val context = LocalContext.current

    val lazyListState = rememberLazyListState()
    val upButtonPadding  = remember {  mutableStateOf ( 10.dp ) }
    val memoListStream = viewModel.memoPagingStream.collectAsLazyPagingItems()

    val isRefreshing = viewModel.isRefreshingStateFlow.collectAsState()

    // val isSearchRefreshing: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing.value,
        onRefresh = {
            SearchQueryData.value.clear()
            viewModel.eventHandler(MemoListViewModel.Event.Search(SearchQueryData))
        }
    )

    LaunchedEffect(key1 = memoListStream.loadState.source.refresh,) {
        channel?.let {
            when (memoListStream.loadState.source.refresh) {
                is LoadState.NotLoading -> {
                    channel.trySend(SnackBarChannelObject.entries.first { item ->
                        item.channelType == SnackBarChannelObject.Type.SEARCH_RESULT
                    }.channel)
                }
                else -> {}
            }
        }

        if(memoListStream.itemCount > 0) {
            scaffoldState.reveal()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(state = pullRefreshState)
    ) {

        when (memoListStream.loadState.source.refresh) {

            is LoadState.Error -> {
                TextButton(
                    modifier = Modifier.align(alignment = Alignment.Center),
                    onClick = {
                        SearchQueryData.value.clear()
                        viewModel.onEvent(MemoListViewModel.Event.Search(SearchQueryData))
                    }
                ) {
                    Text("Error Occurred: Search ReFresh")
                }
            }
            LoadState.Loading -> {

            }
            is LoadState.NotLoading -> {
                LazyColumn(
                    modifier = Modifier,
                    state = lazyListState,
                    userScrollEnabled = true,
                    verticalArrangement = Arrangement.SpaceBetween,
                    contentPadding = PaddingValues(
                        horizontal = 2.dp,
                        vertical = 0.dp
                    )
                ) {
                    items(memoListStream.itemCount) {index ->
                        memoListStream[index]?.let {
                            MemoCompose(
                                item = it,
                                channel = channel,
                                event = viewModel::onEvent,
                                navController = navController
                            )
                        }
                    }
                }

                UpButton(
                    modifier = Modifier
                        .padding(end = 10.dp, bottom = upButtonPadding.value)
                        .align(Alignment.BottomEnd),
                    listState = lazyListState
                )
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing.value,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

}


@Composable
fun UpButton(
    modifier: Modifier,
    listState: LazyListState
){

    val showButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    val coroutineScope = rememberCoroutineScope()
    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current

    if( showButton) {
        FloatingActionButton(
            modifier = Modifier.then(modifier),
            elevation =  FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
            onClick = {
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                    hapticProcessing(coroutineScope, hapticFeedback, isUsableHaptic)
                }
            }
        ) {
            Icon(
                modifier = Modifier.scale(1f),
                imageVector = Icons.Outlined.Publish,
                contentDescription = "Up"
            )
        }
    }
}

@Preview
@Composable
fun PrevMemoListCompose() {
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
                MemoListCompose(
                    navController = navController,
                    MemoListViewModel( repository = repository ),
                    scaffoldState =  rememberBackdropScaffoldState(BackdropValue.Concealed)
                )
            }
        }
    }

}
