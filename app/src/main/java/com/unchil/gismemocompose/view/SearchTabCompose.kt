package com.unchil.gismemocompose.view

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.unchil.gismemocompose.LocalUsableHaptic
import com.unchil.gismemocompose.model.TagInfoDataObject


@Composable
fun AssistChipGroupCompose(
    modifier: Modifier = Modifier,
    isVisible:Boolean =true,
    @SuppressLint("MutableCollectionMutableState") setState: MutableState<ArrayList<Int>> = mutableStateOf( arrayListOf()),
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
                            hapticProcessing(coroutineScope, hapticFeedback, isUsableHaptic)
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



@Composable
fun RadioButtonGroupCompose(
    state:MutableState<Int>,
    data:List<String>,
    layoutScopeType:String = "Row",
    content: @Composable (( ) -> Unit)? = null
){


    val isUsableHaptic = LocalUsableHaptic.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()


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
                                hapticProcessing(coroutineScope, hapticFeedback, isUsableHaptic)
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
                                hapticProcessing(coroutineScope, hapticFeedback, isUsableHaptic)
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

