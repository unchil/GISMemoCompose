package com.unchil.gismemocompose.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Cabin
import androidx.compose.material.icons.outlined.DownhillSkiing
import androidx.compose.material.icons.outlined.EvStation
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.FlightLand
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Hiking
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.Kayaking
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.RollerSkating
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ScubaDiving
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Skateboarding
import androidx.compose.material.icons.outlined.Snowboarding
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import com.unchil.gismemocompose.R

object TagInfoDataObject {
    data class TagInfo(
        var icon : ImageVector,
        var name: Int,
        var isSet: MutableState<Boolean> = mutableStateOf(false)
    )

    val entries:List<TagInfo> = listOf(
        TagInfo(Icons.Outlined.ShoppingCart, R.string.search_hashTag_ShoppingCart),
        TagInfo(Icons.Outlined.AccountBalance, R.string.search_hashTag_AccountBalance),
        TagInfo(Icons.Outlined.Store, R.string.search_hashTag_Store),
        TagInfo(Icons.Outlined.Theaters, R.string.search_hashTag_Theaters),
        TagInfo(Icons.Outlined.FlightTakeoff, R.string.search_hashTag_FlightTakeoff),
        TagInfo(Icons.Outlined.FlightLand, R.string.search_hashTag_FlightLand),
        TagInfo(Icons.Outlined.Hotel, R.string.search_hashTag_Hotel),
        TagInfo(Icons.Outlined.School, R.string.search_hashTag_School),
        TagInfo(Icons.Outlined.Hiking, R.string.search_hashTag_Hiking),
        TagInfo(Icons.Outlined.DownhillSkiing, R.string.search_hashTag_DownhillSkiing),
        TagInfo(Icons.Outlined.Kayaking, R.string.search_hashTag_Kayaking),
        TagInfo(Icons.Outlined.Skateboarding, R.string.search_hashTag_Skateboarding),
        TagInfo(Icons.Outlined.Snowboarding, R.string.search_hashTag_Snowboarding),
        TagInfo(Icons.Outlined.ScubaDiving, R.string.search_hashTag_ScubaDiving),
        TagInfo(Icons.Outlined.RollerSkating, R.string.search_hashTag_RollerSkating),
        TagInfo(Icons.Outlined.Photo, R.string.search_hashTag_Photo),
        TagInfo(Icons.Outlined.Restaurant, R.string.search_hashTag_Restaurant),
        TagInfo(Icons.Outlined.Park, R.string.search_hashTag_Park),
        TagInfo(Icons.Outlined.LocalCafe, R.string.search_hashTag_LocalCafe),
        TagInfo(Icons.Outlined.LocalTaxi, R.string.search_hashTag_LocalTaxi),
        TagInfo(Icons.Outlined.Forest, R.string.search_hashTag_Forest),
        TagInfo(Icons.Outlined.EvStation, R.string.search_hashTag_EvStation),
        TagInfo(Icons.Outlined.FitnessCenter, R.string.search_hashTag_FitnessCenter),
        TagInfo(Icons.Outlined.House, R.string.search_hashTag_House),
        TagInfo(Icons.Outlined.Apartment, R.string.search_hashTag_Apartment),
        TagInfo(Icons.Outlined.Cabin, R.string.search_hashTag_Cabin)
    ).sortedBy {
        it.name
    }

    fun clear(){
        entries.forEach {
            it.isSet.value = false
        }
    }
}
