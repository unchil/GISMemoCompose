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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import com.unchil.gismemocompose.navigation.mainScreens
import com.unchil.gismemocompose.navigation.navigateTo
import com.unchil.gismemocompose.shared.ChkNetWork
import com.unchil.gismemocompose.shared.checkInternetConnected
import com.unchil.gismemocompose.shared.composables.LocalPermissionsManager
import com.unchil.gismemocompose.shared.composables.PermissionsManager
import com.unchil.gismemocompose.ui.theme.GISMemoTheme
import com.unchil.gismemocompose.view.GisMemoNavHost
import com.unchil.gismemocompose.view.getLanguageArray
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

        setContent {

            val context = LocalContext.current
            val onChangeLocale = repository.onChangeLocale.collectAsState()
            val isUsableHaptic = repository.isUsableHaptic.collectAsState()
            val isUsableDarkMode = repository.isUsableDarkMode.collectAsState()
            val isUsableDynamicColor = repository.isUsableDynamicColor.collectAsState()
            val hapticFeedback = LocalHapticFeedback.current
            val isPressed = remember { mutableStateOf(false) }


            LaunchedEffect(key1 = isPressed.value, key2 = isUsableHaptic.value) {
                if (isPressed.value && isUsableHaptic.value) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isPressed.value = false
                }
            }

            val coroutineScope = rememberCoroutineScope()

            val navController = rememberNavController()
            val currentBackStack by navController.currentBackStackEntryAsState()

            val configuration = LocalConfiguration.current

            val selectedItem = rememberSaveable { mutableStateOf(0) }
            var isPortrait by remember { mutableStateOf(false) }

            var gridWidth by remember { mutableStateOf(1f) }

            when (configuration.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    isPortrait = true
                    gridWidth = 1f
                }
                else ->{
                    isPortrait = false
                    gridWidth = 0.9f
                }
            }


            LaunchedEffect(key1 = currentBackStack){
                val currentScreen = mainScreens.find {
                    it.route ==  currentBackStack?.destination?.route
                }
                selectedItem.value =  mainScreens.indexOf(currentScreen)
            }

            var isConnect  by remember { mutableStateOf(context.checkInternetConnected()) }

            LaunchedEffect(key1 = isConnect ){
                while(!isConnect) {
                    delay(500)
                    isConnect = context.checkInternetConnected()
                }
            }


            GISMemoTheme(darkTheme = isUsableDarkMode.value,
                dynamicColor = isUsableDynamicColor.value
            ) {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    CompositionLocalProvider(
                        LocalChangeLocale provides onChangeLocale.value,
                        LocalUsableDarkMode provides isUsableDarkMode.value,
                        LocalUsableDynamicColor provides isUsableDynamicColor.value,
                        LocalUsableHaptic provides isUsableHaptic.value,
                        LocalRepository provides repository,
                        LocalPermissionsManager provides permissionsManager
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (!isConnect) {
                                ChkNetWork(
                                    onCheckState = {
                                        coroutineScope.launch {
                                            isConnect =
                                                checkInternetConnected()
                                        }
                                    }
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top
                                ) {

                                    if (isPortrait) {

                                        BottomNavigation(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(60.dp)
                                                .shadow(elevation = 1.dp),
                                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                                        ) {

                                            Spacer(
                                                modifier = Modifier.padding(
                                                    horizontal = 10.dp
                                                )
                                            )

                                            mainScreens.forEachIndexed { index, it ->

                                                BottomNavigationItem(
                                                    icon = {
                                                        Icon(
                                                            imageVector = it.icon
                                                                ?: Icons.Outlined.Info,
                                                            contentDescription = context.resources.getString(
                                                                it.name
                                                            ),
                                                            tint = if (selectedItem.value == index) Color.Red else MaterialTheme.colorScheme.secondary
                                                        )
                                                    },
                                                    label = {
                                                        androidx.compose.material.Text(
                                                            context.resources.getString(
                                                                it.name
                                                            )
                                                        )
                                                    },
                                                    selected = selectedItem.value == index,
                                                    onClick = {
                                                        isPressed.value =
                                                            true
                                                        selectedItem.value =
                                                            index
                                                        navController.navigateTo(
                                                            mainScreens[index].route
                                                        )
                                                    },
                                                    selectedContentColor = Color.Red,
                                                    unselectedContentColor = MaterialTheme.colorScheme.secondary

                                                )

                                            }

                                            Spacer(
                                                modifier = Modifier.padding(
                                                    horizontal = 10.dp
                                                )
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier,
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Box(
                                            modifier = Modifier.fillMaxWidth(
                                                gridWidth
                                            )
                                        ) {
                                            GisMemoNavHost(navController)
                                        }


                                        if (!isPortrait) {

                                            NavigationRail(
                                                modifier = Modifier.shadow(
                                                    elevation = 1.dp
                                                )
                                                    .width(80.dp),
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            ) {

                                                Spacer(
                                                    modifier = Modifier.padding(
                                                        vertical = 20.dp
                                                    )
                                                )
                                                mainScreens.forEachIndexed { index, it ->
                                                    NavigationRailItem(
                                                        icon = {
                                                            Icon(
                                                                imageVector = it.icon
                                                                    ?: Icons.Outlined.Info,
                                                                contentDescription = context.resources.getString(
                                                                    it.name
                                                                ),
                                                                tint = if (selectedItem.value == index) Color.Red else MaterialTheme.colorScheme.secondary
                                                            )
                                                        },
                                                        label = {
                                                            Text(
                                                                text = context.resources.getString(
                                                                    it.name
                                                                ),
                                                                color = if (selectedItem.value == index) Color.Red else MaterialTheme.colorScheme.secondary
                                                            )
                                                        },
                                                        selected = selectedItem.value == index,
                                                        onClick = {
                                                            isPressed.value =
                                                                true
                                                            selectedItem.value =
                                                                index
                                                            navController.navigateTo(
                                                                mainScreens[index].route
                                                            )
                                                        }
                                                    )
                                                }
                                                Spacer(
                                                    modifier = Modifier.padding(
                                                        vertical = 20.dp
                                                    )
                                                )
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

}
