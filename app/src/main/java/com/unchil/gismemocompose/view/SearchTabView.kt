package com.unchil.gismemocompose.view

import android.annotation.SuppressLint
import android.app.Activity
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unchil.gismemocompose.LocalUsableHaptic
import com.unchil.gismemocompose.R
import com.unchil.gismemocompose.model.TagInfoDataObject
import com.unchil.gismemocompose.shared.composables.LocalPermissionsManager
import com.unchil.gismemocompose.shared.composables.PermissionsManager
import com.unchil.gismemocompose.ui.theme.GISMemoTheme
import com.unchil.gismemocompose.viewmodel.ListViewModel
import kotlinx.coroutines.launch

typealias QueryData= Pair<SearchOption, SearchQueryDataValue>


enum class SearchOption {
    TITLE, SECRET, MARKER, TAG, DATE
}

fun SearchOption.name():String{
    return when(this){
        SearchOption.TITLE ->  "제목"
        SearchOption.SECRET -> "보안"
        SearchOption.MARKER -> "마커"
        SearchOption.TAG -> "태그"
        SearchOption.DATE -> "날짜"
    }
}

sealed class SearchQueryDataValue {
    data class radioGroupOption(val index:Int) : SearchQueryDataValue()
    data class tagOption(val indexList: ArrayList<Int>): SearchQueryDataValue()
    data class dateOption(val fromToDate:Pair<Long,Long>): SearchQueryDataValue()
    data class titleOption(val title: String): SearchQueryDataValue()
}

@Composable
fun RadioButtonGroupView(
    state:MutableState<Int>,
    data:List<String>,
    layoutScopeType:String = "Row",
    content: @Composable (( ) -> Unit)? = null
){

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

    val (selectedOption, onOptionSelected) = mutableStateOf(data[state.value])

    if(layoutScopeType == "Column"){


        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            itemsIndexed(data){index, it ->
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = (it == selectedOption),
                            onClick = {
                                hapticProcessing()
                                onOptionSelected( it )
                                state.value = index
                            },
                            role = Role.RadioButton
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (it == selectedOption),
                        onClick = null
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                    Text(
                        text = it,
                        modifier = Modifier,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

            }

        }



    } else {
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            content?.let {
                it()
            }

            data.forEachIndexed { index, it ->
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = (it == selectedOption),
                            onClick = {
                                hapticProcessing()
                                onOptionSelected( it )
                                state.value = index
                            },
                            role = Role.RadioButton
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (it == selectedOption),
                        onClick = null
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                    Text(
                        text = it,
                        modifier = Modifier,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }


    }


}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
    isSearchRefreshing:MutableState<Boolean> = mutableStateOf( false),
    sheetControl: (() -> Unit)? = null,
    onEvent: ((ListViewModel.Event) -> Unit)? = null,
    onMessage:(() -> Unit)? = null
){

    val context = LocalContext.current
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



    val isVisible:MutableState<Boolean> = remember { mutableStateOf(true) }

    val dateRangePickerState = rememberDateRangePickerState()

    val secretOption =  listOf(
        context.resources.getString(R.string.search_radioBt_select),
        context.resources.getString(R.string.search_radioBt_none),
        context.resources.getString(R.string.search_radioBt_all)
    )
    val markerOption =  listOf(
        context.resources.getString(R.string.search_radioBt_select),
        context.resources.getString(R.string.search_radioBt_none),
        context.resources.getString(R.string.search_radioBt_all)
    )


    val secretRadioGroupState = rememberSaveable {
        mutableStateOf(secretOption.lastIndex )
    }

    val markerRadioGroupState = rememberSaveable{
        mutableStateOf(markerOption.lastIndex )
    }



    var isTagBox by rememberSaveable{  mutableStateOf(false)}
    val isDateBox = rememberSaveable{  mutableStateOf(false)}

    val selectedTagArray:MutableState<ArrayList<Int>> = rememberSaveable{ mutableStateOf(arrayListOf())  }
    val query_title = rememberSaveable { mutableStateOf("") }


    val startLauncherRecognizerIntent = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        if (it.resultCode == Activity.RESULT_OK) {
            val result =  it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            query_title.value = query_title.value + result?.get(0).toString() + " "
        }
    }

    val initStateValue = {
        query_title.value = ""
        dateRangePickerState.setSelection(null, null)
        secretRadioGroupState.value =  secretOption.lastIndex
        markerRadioGroupState.value = markerOption.lastIndex
        selectedTagArray.value = arrayListOf()
    }

    val onSearch: (String) -> Unit = { searchTitle ->

        val queryDataList =  mutableListOf<QueryData>()

        searchTitle.trim().let { queryString ->
            if (queryString.isNotEmpty()) {
                queryDataList.add(
                    QueryData(
                        SearchOption.TITLE,
                        SearchQueryDataValue.titleOption(title = queryString )
                    )
                )
            }
        }


        dateRangePickerState.selectedStartDateMillis?.let {
            if(it != 0L){
                QueryData(
                    SearchOption.DATE,
                    SearchQueryDataValue.dateOption(
                        fromToDate = Pair( dateRangePickerState.selectedStartDateMillis ?: 0, dateRangePickerState.selectedEndDateMillis ?: 0)
                    )
                )
            }
        }

        if( secretRadioGroupState.value  <  secretOption.lastIndex){
            queryDataList.add(
                QueryData(
                    SearchOption.SECRET,
                    SearchQueryDataValue.radioGroupOption(
                        index = secretRadioGroupState.value
                    )
                )
            )
        }

        if(markerRadioGroupState.value < markerOption.lastIndex){
            queryDataList.add(
                QueryData(
                    SearchOption.MARKER,
                    SearchQueryDataValue.radioGroupOption(
                        index = markerRadioGroupState.value
                    )
                )
            )
        }

        if( selectedTagArray.value.isNotEmpty()){
            queryDataList.add(
                QueryData(
                    SearchOption.TAG,
                    SearchQueryDataValue.tagOption(
                        indexList =  selectedTagArray.value
                    )
                )
            )
        }



        onEvent?.let {
            it(ListViewModel.Event.Search(queryDataList))
            if(queryDataList.isNotEmpty()){
                isSearchRefreshing.value= true
            }

        }

        initStateValue()

        sheetControl?.let {
            it()
        }

    }

    val scrollState = rememberScrollState()

    /*
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val isPressed by interactionSource.collectIsPressedAsState()

     */

// context.resources.getString(R.string.search_searchBar_placeholder)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(color = MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.padding(vertical = 2.dp))

            SearchBar(
                query = query_title.value,
                onQueryChange = {
                    query_title.value = it
                },

                onSearch = onSearch,
                active = isVisible.value,
                onActiveChange = {
                    isVisible.value = it
                },
                placeholder = {
                    Text(
                        text = context.resources.getString(R.string.search_searchBar_placeholder) ,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(horizontal = 6.dp)
                    .padding(top = 0.dp)
                    .clip(shape = ShapeDefaults.Small),
                leadingIcon = {


                    IconButton(
                        modifier = Modifier,
                        onClick = {
                            hapticProcessing()
                            onSearch(query_title.value)
                        },
                        content = {
                            Icon(
                                modifier = Modifier,
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search"
                            )
                        }
                    )


                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            modifier = Modifier,
                            onClick = {
                                hapticProcessing()
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
                            modifier = Modifier,
                            onClick = {
                                hapticProcessing()
                                initStateValue()
                                onMessage?.let {
                                    it()
                                }
                            },
                            content = {
                                Icon(
                                    modifier = Modifier,
                                    imageVector = Icons.Outlined.Replay,
                                    contentDescription = "Clear"
                                )
                            }
                        )


                    }
                },
                //shape = ShapeDefaults.Medium,
                tonalElevation = 6.dp,
            ) { }


        HorizontalDivider(
            Modifier
                .fillMaxWidth()
                .padding(6.dp)
        )



            RadioButtonGroupView(
                state = secretRadioGroupState,
                data = secretOption
            ) {
                Row(modifier = Modifier) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Secret"
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                    Text(
                        modifier = Modifier,
                        text = context.resources.getString(R.string.search_radioBtGroup_secret),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

        HorizontalDivider(
            Modifier
                .fillMaxWidth()
                .padding(6.dp)
        )

            RadioButtonGroupView(
                state = markerRadioGroupState,
                data = markerOption
            ) {
                Row(modifier = Modifier) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "marker"
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                    Text(
                        modifier = Modifier,
                        text =  context.resources.getString(R.string.search_radioBtGroup_marker),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

        HorizontalDivider(
            Modifier
                .fillMaxWidth()
                .padding(6.dp)
        )


            androidx.compose.material.IconButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = {
                    hapticProcessing()
                    isTagBox = !isTagBox
                          },
                content = {
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Class,
                            contentDescription = "tag"
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                        Text(text = context.resources.getString(R.string.search_hashTag),
                            style = MaterialTheme.typography.titleSmall)
                        Icon(
                            modifier = Modifier.scale(1f),
                            imageVector = if (isTagBox) Icons.Outlined.ArrowDropDown else Icons.AutoMirrored.Outlined.ArrowRight,
                            contentDescription = "tag "
                        )
                    }
                })



            AssistChipGroupView(
                isVisible = isTagBox,
                setState = selectedTagArray,
            )



        HorizontalDivider(
            Modifier
                .fillMaxWidth()
                .padding(6.dp)
        )


            androidx.compose.material.IconButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = {
                    hapticProcessing()
                    isDateBox.value = !isDateBox.value
                          },
                content = {
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "date"
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                        Text(text = context.resources.getString(R.string.search_period),
                            style = MaterialTheme.typography.titleSmall)
                        Icon(
                            modifier = Modifier.scale(1f),
                            imageVector = if (isDateBox.value)  Icons.Outlined.ArrowDropDown else Icons.AutoMirrored.Outlined.ArrowRight,
                            contentDescription = "date "
                        )
                    }
                })

            AnimatedVisibility(visible = isDateBox.value) {

                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier
                        .height(420.dp),
                    title = { Text(text = "", style = MaterialTheme.typography.bodySmall) },
                    headline = {
                        Text(
                            modifier = Modifier.padding(start = 10.dp),
                            text = context.resources.getString(R.string.search_dateRangePicker_headline),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )

            }

        }

}





@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssistChipGroupView(
    modifier: Modifier = Modifier,
    isVisible:Boolean =true,
    @SuppressLint("MutableCollectionMutableState") setState:MutableState<ArrayList<Int>> = mutableStateOf( arrayListOf()),
    content: @Composable (( ) -> Unit)? = null
){

    val context = LocalContext.current

    TagInfoDataObject.clear()
    setState.value.forEach {
        TagInfoDataObject.entries[it].isSet.value = true
    }

    val  lazyStaggeredGridState = rememberLazyStaggeredGridState()
    val itemModifier = Modifier.wrapContentSize()
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

    AnimatedVisibility(visible = isVisible) {
        Column (
            modifier = Modifier.then(modifier)
        ){
            LazyHorizontalStaggeredGrid(
                rows =  StaggeredGridCells.Fixed(4),
                modifier  = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .height(200.dp),
                state = lazyStaggeredGridState,
                contentPadding =  PaddingValues(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalItemSpacing = 6.dp,
                userScrollEnabled = true,
            ){
                itemsIndexed(TagInfoDataObject.entries) { index, it ->
                    AssistChip(
                        modifier = itemModifier,
                        shape = ShapeDefaults.ExtraSmall,
                        onClick = {
                            hapticProcessing()
                            it.isSet.value = !it.isSet.value
                            if (it.isSet.value)  setState.value.add(index) else   setState.value.remove(index)
                        },
                        label = {
                            Row (verticalAlignment = Alignment.CenterVertically){
                                Icon(
                                    imageVector = it.icon,
                                    contentDescription = "",
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                                Text(
                                    text = context.resources.getString(it.name),
                                style = MaterialTheme.typography.labelMedium)
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector =   if (it.isSet.value) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                contentDescription = "",
                                modifier = Modifier.size(AssistChipDefaults.IconSize),

                            )
                        },
                    )
                } // itemsIndexed
            }
            content?.let {
                it()
            }
        }
    }
}




@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssistChipGroupViewNew(
    modifier: Modifier = Modifier,
    isVisible:Boolean =true,
    setState:MutableState<ArrayList<Int>> = mutableStateOf( arrayListOf()),
    content: @Composable (( ) -> Unit)? = null
){

    val context = LocalContext.current

    TagInfoDataObject.clear()
    setState.value.forEach {
        TagInfoDataObject.entries[it].isSet.value = true
    }

    val  lazyStaggeredGridState = rememberLazyStaggeredGridState()
    val itemModifier = Modifier.wrapContentSize()
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

    AnimatedVisibility(visible = isVisible) {
        Column (
            modifier = Modifier.then(modifier)
        ){
            LazyHorizontalStaggeredGrid(
                rows =  StaggeredGridCells.Fixed(4),
                modifier  = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth()
                    .height(200.dp),
                state = lazyStaggeredGridState,
                contentPadding =  PaddingValues(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalItemSpacing = 6.dp,
                userScrollEnabled = true,
            ){
                itemsIndexed(TagInfoDataObject.entries) { index, it ->
                    AssistChip(
                        modifier = itemModifier,
                        shape = ShapeDefaults.ExtraSmall,
                        onClick = {
                            hapticProcessing()
                            it.isSet.value = !it.isSet.value
                            if (it.isSet.value)  setState.value.add(index) else   setState.value.remove(index)
                        },
                        label = {
                            Row (verticalAlignment = Alignment.CenterVertically){
                                Icon(
                                    imageVector = it.icon,
                                    contentDescription = "",
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                                Text(
                                    text = context.resources.getString(it.name),
                                    style = MaterialTheme.typography.labelMedium)
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector =   if (it.isSet.value) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                contentDescription = "",
                                modifier = Modifier.size(AssistChipDefaults.IconSize),

                                )
                        },
                    )
                } // itemsIndexed
            }
            content?.let {
                it()
            }
        }
    }
}




@SuppressLint("UnrememberedMutableState", "MutableCollectionMutableState")
@Preview
@Composable
private fun PrevSearchView(
    modifier: Modifier = Modifier,
){

    val selectedTags = mutableStateOf( arrayListOf<Int>() )

    val permissionsManager = PermissionsManager()
    CompositionLocalProvider(LocalPermissionsManager provides permissionsManager) {


        GISMemoTheme {
            Surface(
                modifier = Modifier.background(color = Color.White)
            ) {

                AssistChipGroupViewNew(
                    setState = selectedTags
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()

                    ) {

                        HorizontalDivider()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {


                            IconButton(
                                modifier = Modifier,
                                onClick = {
                                    selectedTags.value.clear()
                                },
                                content = {
                                    Icon(
                                        modifier = Modifier,
                                        imageVector = Icons.Outlined.Replay,
                                        contentDescription = "Clear"
                                    )
                                }
                            )

                        }


                    }
                }

            }
        }


    }

}

