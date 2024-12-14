package com.unchil.gismemocompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unchil.gismemocompose.data.Repository
import kotlinx.coroutines.launch

class SpeechToTextViewModel (  val repository: Repository ) : ViewModel(){


    var _currentAudioText: MutableList<Pair<String, List<String>>> = mutableListOf()

    init {
        _currentAudioText = repository.currentAudioText.value.toMutableList()
    }


    fun onEvent(event: Event){
        when(event){

            is Event.SetAudioText -> {
                setAudioText(event.data)
            }
        }
    }

    private fun setAudioText(data: List<Pair<String, List<String>>>){
        viewModelScope.launch {
            repository.setAudioText(data)
        }
    }


    sealed class Event {
        data class SetAudioText(val data: List<Pair<String,  List<String>>>): Event()

    }


}