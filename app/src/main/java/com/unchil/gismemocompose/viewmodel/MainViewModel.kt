package com.unchil.gismemocompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unchil.gismemocompose.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(
    val repository: Repository
) : ViewModel() {


    private val _isFirstSetup: MutableStateFlow<Boolean>
            = MutableStateFlow(true)

    val isFirstSetup: StateFlow<Boolean>
            = _isFirstSetup.asStateFlow()


    private val _isChangeLocale: MutableStateFlow<Int>
            = MutableStateFlow(0)

    val isChangeLocale: StateFlow<Int>
            = _isChangeLocale

    val _isUsableDarkMode: MutableStateFlow<Boolean>
            = MutableStateFlow(false)

    val isUsableDarkMode: StateFlow<Boolean>
            = _isUsableDarkMode.asStateFlow()

    val _isUsableDynamicColor: MutableStateFlow<Boolean>
            = MutableStateFlow(false)

    val isUsableDynamicColor: StateFlow<Boolean>
            = _isUsableDynamicColor.asStateFlow()

    val _isUsableHaptic: MutableStateFlow<Boolean>
            = MutableStateFlow(true)

    val isUsableHaptic: StateFlow<Boolean>
            = _isUsableHaptic.asStateFlow()


    // locale 실시간 반영을 위한 state
    val _realTimeChangeLocale: MutableStateFlow<Boolean>
            = MutableStateFlow(false)

    val realTimeChangeLocale: StateFlow<Boolean>
            = _realTimeChangeLocale.asStateFlow()


    init {
        viewModelScope.launch {
            repository.isFirstSetup.collectLatest { it ->
                _isFirstSetup.value = it
            }
        }

        viewModelScope.launch {
            repository.isChangeLocale.collectLatest { it ->
                _isChangeLocale.value = it
            }
        }

        viewModelScope.launch {
            repository.isUsableDarkMode.collectLatest { it ->
                _isUsableDarkMode.value = it
            }
        }

        viewModelScope.launch {
            repository.isUsableDynamicColor.collectLatest { it ->
                _isUsableDynamicColor.value = it
            }
        }

        viewModelScope.launch {
            repository.isUsableHaptic.collectLatest { it ->
                _isUsableHaptic.value = it
            }
        }

        viewModelScope.launch {
            repository.realTimeChangeLocale.collectLatest { it ->
                _realTimeChangeLocale.value = it
            }
        }
    }

    fun onEvent(event: Event) {
        when(event){
            is Event.UpdateIsChangeLocale -> {
                updateIsChangeLocale(event.isChnageLocale)
            }
            is Event.UpdateIsFirstSetup -> {
                updateIsFirstSetup(event.isFirstSetup)
            }
        }
    }

    private fun updateIsFirstSetup (isFirstSetup:Boolean) {
        repository.updateIsFirstSetup(isFirstSetup)
    }


    private fun updateIsChangeLocale (isChangeLocale:Int) {
        repository.updateIsChangeLocale(isChangeLocale)
    }


    sealed class Event {

        data class UpdateIsFirstSetup(val isFirstSetup:Boolean): Event()

        data class UpdateIsChangeLocale(val isChnageLocale:Int): Event()

    }

}