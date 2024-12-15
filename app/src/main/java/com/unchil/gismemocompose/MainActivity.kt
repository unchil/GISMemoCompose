package com.unchil.gismemocompose

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unchil.gismemocompose.data.LocalRepository
import com.unchil.gismemocompose.data.Repository
import com.unchil.gismemocompose.data.RepositoryProvider
import com.unchil.gismemocompose.db.LuckMemoDB
import com.unchil.gismemocompose.model.MainTabObject
import com.unchil.gismemocompose.navigation.navigateTo
import com.unchil.gismemocompose.shared.ChkNetWork
import com.unchil.gismemocompose.shared.checkInternetConnected
import com.unchil.gismemocompose.shared.composables.LocalPermissionsManager
import com.unchil.gismemocompose.shared.composables.PermissionsManager
import com.unchil.gismemocompose.ui.theme.GISMemoTheme
import com.unchil.gismemocompose.view.GisMemoNavHost
import com.unchil.gismemocompose.view.getLanguageArray
import com.unchil.gismemocompose.view.hapticProcessing
import com.unchil.gismemocompose.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// SettingScreen 에서의 locale 변경시 실시간으로 SettingScreen 에 ReCompose 를 유도하기 위해
val LocalChangeLocale = compositionLocalOf{ false }
val LocalUsableHaptic = compositionLocalOf{ true }
val LocalUsableDarkMode = compositionLocalOf{ false }
val LocalUsableDynamicColor = compositionLocalOf{ false }


class MainActivity : ComponentActivity() {

    private val permissionsManager = PermissionsManager()

    lateinit var repository:Repository


    override fun attachBaseContext(context: Context?) {

        if(context != null ){

            val luckMemoDB = LuckMemoDB.getInstance(context.applicationContext)
            repository = RepositoryProvider.getRepository().apply { database = luckMemoDB }
            val viewModel =  MainViewModel( repository = repository )

            if(viewModel.isFirstSetup.value){
                viewModel.onEvent(MainViewModel.Event.UpdateIsFirstSetup(false))
                val index = context.getLanguageArray().indexOf(Locale.getDefault().language)
                viewModel.onEvent(MainViewModel.Event.UpdateIsChangeLocale(if (index == -1 ) 0 else index))
                super.attachBaseContext(context)
            } else {
                val locale = Locale( context.getLanguageArray()[viewModel.isChangeLocale.value] )
                Locale.setDefault(locale)
                context.resources.configuration.setLayoutDirection(locale)
                context.resources.configuration.setLocale(locale)
                context.createConfigurationContext(context.resources.configuration)
                super.attachBaseContext(context.createConfigurationContext(context.resources.configuration))
            }


        }else {
            super.attachBaseContext(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //    KakaoSdk.init(this, BuildConfig.KAKAO_KEY_KEY)
        val viewModel =  MainViewModel( repository = repository )
        setContent {

            val isUsableHapticState = viewModel.isUsableHaptic.collectAsState()
            val isUsableDarkModeState = viewModel.isUsableDarkMode.collectAsState()
            val isUsableDynamicColorState = viewModel.isUsableDynamicColor.collectAsState()
            val realTimeChangeLocale = viewModel.realTimeChangeLocale.collectAsState()

            CompositionLocalProvider(
                LocalChangeLocale provides realTimeChangeLocale.value,
                LocalPermissionsManager provides permissionsManager,
                LocalRepository provides repository,
                LocalUsableHaptic provides isUsableHapticState.value,
                LocalUsableDarkMode provides isUsableDarkModeState.value,
                LocalUsableDynamicColor provides isUsableDynamicColorState.value,
            ){

                val context = LocalContext.current
                val configuration = LocalConfiguration.current
                val hapticFeedback = LocalHapticFeedback.current
                val isUsableDarkMode = LocalUsableDarkMode.current
                val isUsableDynamicColor = LocalUsableDynamicColor.current
                val isUsableHaptic = LocalUsableHaptic.current

                val coroutineScope = rememberCoroutineScope()
                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val selectedItem = rememberSaveable { mutableIntStateOf(0) }
                val isPortrait = remember { mutableStateOf(false) }
                val gridWidth = remember { mutableFloatStateOf(1f) }



                when (configuration.orientation) {
                    Configuration.ORIENTATION_PORTRAIT -> {
                        isPortrait.value = true
                        gridWidth.floatValue = 1f
                    }

                    else -> {
                        isPortrait.value = false
                        gridWidth.floatValue = 0.9f
                    }
                }


                LaunchedEffect(key1 = currentBackStack) {
                    val currentScreen = MainTabObject.Types.find {
                        it.route == currentBackStack?.destination?.route
                    }
                    selectedItem.intValue = MainTabObject.Types.indexOf(currentScreen)
                }

                val isConnect = remember { mutableStateOf(context.checkInternetConnected()) }

                LaunchedEffect(key1 = isConnect.value) {
                    while (!isConnect.value) {
                        delay(500)
                        isConnect.value = context.checkInternetConnected()
                    }
                }


                GISMemoTheme(
                    darkTheme = isUsableDarkMode,
                    dynamicColor = isUsableDynamicColor
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {

                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (!isConnect.value) {
                                ChkNetWork(
                                    onCheckState = {
                                        coroutineScope.launch {
                                            isConnect.value = checkInternetConnected()
                                        }
                                    }
                                )
                            } else {

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top
                                ) {
                                    if (isPortrait.value) {
                                        BottomNavigation(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(60.dp)
                                                .shadow(elevation = 1.dp),
                                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                                        ) {

                                            Spacer(Modifier.padding(horizontal = 10.dp))

                                            MainTabObject.Types.forEachIndexed { index, gisMemoDestinations ->
                                                BottomNavigationItem(
                                                    icon = {
                                                        Icon(
                                                            imageVector = gisMemoDestinations.icon
                                                                ?: Icons.Outlined.Info,
                                                            contentDescription = context.resources.getString(
                                                                gisMemoDestinations.name
                                                            ),
                                                            tint = if (selectedItem.intValue == index) MaterialTheme.colorScheme.scrim
                                                            else MaterialTheme.colorScheme.outline
                                                        )
                                                    },
                                                    label = {
                                                        Text(
                                                            text = context.resources.getString(
                                                                gisMemoDestinations.name
                                                            ),
                                                            color = if (selectedItem.intValue == index) MaterialTheme.colorScheme.scrim
                                                            else MaterialTheme.colorScheme.outline
                                                        )
                                                    },
                                                    selected = selectedItem.intValue == index,
                                                    onClick = {
                                                        hapticProcessing(coroutineScope, hapticFeedback, isUsableHaptic)
                                                        selectedItem.intValue = index
                                                        navController.navigateTo(MainTabObject.Types[index].route)
                                                    },
                                                    selectedContentColor = Color.Red,
                                                    unselectedContentColor = MaterialTheme.colorScheme.secondary
                                                )
                                            }

                                            Spacer(Modifier.padding(horizontal = 10.dp))
                                        }
                                    }

                                    Row(
                                        modifier = Modifier,
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        if (!isPortrait.value) {
                                            NavigationRail(
                                                modifier = Modifier
                                                    .shadow(elevation = 1.dp)
                                                    .width(80.dp),
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            ) {

                                                Spacer(Modifier.padding(vertical = 20.dp))

                                                MainTabObject.Types.forEachIndexed { index, gisMemoDestinations ->
                                                    NavigationRailItem(
                                                        icon = {
                                                            Icon(
                                                                imageVector = gisMemoDestinations.icon
                                                                    ?: Icons.Outlined.Info,
                                                                contentDescription = context.resources.getString(
                                                                    gisMemoDestinations.name
                                                                ),
                                                                tint = if (selectedItem.intValue == index) Color.Red else MaterialTheme.colorScheme.secondary
                                                            )
                                                        },
                                                        label = {
                                                            Text(
                                                                text = context.resources.getString(
                                                                    gisMemoDestinations.name
                                                                ),
                                                                color = if (selectedItem.intValue == index) Color.Red else MaterialTheme.colorScheme.secondary
                                                            )
                                                        },
                                                        selected = selectedItem.intValue == index,
                                                        onClick = {
                                                            hapticProcessing(coroutineScope, hapticFeedback, isUsableHaptic)
                                                            selectedItem.intValue = index
                                                            navController.navigateTo(MainTabObject.Types[index].route)
                                                        }
                                                    )
                                                }

                                                Spacer(Modifier.padding(vertical = 20.dp))
                                            }
                                        }

                                        Box(modifier = Modifier.fillMaxWidth(gridWidth.floatValue)) {
                                            GisMemoNavHost(navController = navController)
                                        }
                                    }
                                }
                            }

                        }

                    }
                }


            }

        }
    }

}
