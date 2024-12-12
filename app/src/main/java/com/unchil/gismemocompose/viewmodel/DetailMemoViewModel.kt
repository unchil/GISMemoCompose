package com.unchil.gismemocompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.unchil.gismemocompose.data.Repository
import com.unchil.gismemocompose.db.entity.MEMO_TBL
import com.unchil.gismemocompose.db.entity.MEMO_WEATHER_TBL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DetailMemoViewModel (val repository: Repository, ) : ViewModel() {

    private val _memo: MutableStateFlow<MEMO_TBL?>
            = MutableStateFlow(null)
    val memo: StateFlow<MEMO_TBL?>
            = _memo

    private val _weather: MutableStateFlow<MEMO_WEATHER_TBL?>
            = MutableStateFlow(null)
    val weather: StateFlow<MEMO_WEATHER_TBL?>
            = _weather


    private val _tagArrayList: MutableStateFlow<ArrayList<Int>>
            = MutableStateFlow(arrayListOf())
    val tagArrayList: StateFlow<ArrayList<Int>>
            = _tagArrayList


    init {
        viewModelScope.launch {
            repository.selectedMemo.collectLatest {
                _memo.value = it
            }
        }

        viewModelScope.launch {
            repository.selectedWeather.collectLatest {
                _weather.value = it
            }
        }

        viewModelScope.launch {
            repository.selectedTagList.collectLatest {
                _tagArrayList.value = it
            }
        }
    }




    fun onEvent(event: Event) {
        when (event) {
            is Event.SetMemo ->  setMemo(event.id)
            is Event.SetFiles -> setFiles(event.id)
            is Event.SetTags -> setTags(event.id)
            is Event.SetWeather -> setWeather(event.id)
            is Event.ToRoute -> {
                toRoute(route = event.route, navController = event.navController)
            }
            is Event.UpdateIsMark -> {
                updateMark(event.id, event.isMark)
            }
            is Event.UpdateIsSecret -> {
                updateSecret(event.id, event.isSecret)
            }
            is Event.UpdateTagList ->{
                updateTagList(event.id, event.selectTagList, event.snippets)
            }

            is Event.SetDetailMemo -> {
                setDetailMemo(event.id)
            }
        }

    }

    private fun setDetailMemo(id:Long){
        viewModelScope.launch {
            repository.setMemo(id = id)
        }
        viewModelScope.launch {
            repository.setFiles(id = id)
        }

        viewModelScope.launch {
            repository.setTags(id = id)
        }

        viewModelScope.launch {
            repository.setWeather(id = id)
        }
    }

    private fun updateTagList(id:Long, selectTagList:  ArrayList<Int>, snippets:String){
        viewModelScope.launch {
            repository.updateTagList(id, selectTagList, snippets)
        }
    }



    private fun updateMark(id:Long, isMark: Boolean){
        viewModelScope.launch {
            repository.updateMark(id, isMark)
        }
    }
    private fun updateSecret(id:Long, isSecret: Boolean){
        viewModelScope.launch {
            repository.updateSecret(id, isSecret)
        }
    }


    private fun toRoute(navController: NavController, route:String){
        navController.navigate(route = route)
    }

    private fun setFiles(id:Long){
        viewModelScope.launch {
            repository.setFiles(id = id)
        }
    }

    private fun setMemo(id:Long){
        viewModelScope.launch {
            repository.setMemo(id = id)
        }
    }
    private fun setTags(id:Long){
        viewModelScope.launch {
            repository.setTags(id = id)
        }
    }
    private fun setWeather(id:Long){
        viewModelScope.launch {
            repository.setWeather(id = id)
        }
    }


    sealed class Event {
        data class  SetMemo(val id: Long): Event()
        data class  SetWeather(val id: Long): Event()
        data class  SetTags(val id: Long): Event()
        data class  SetFiles(val id: Long): Event()
        data class SetDetailMemo(val id:Long): Event()
        data class UpdateIsSecret(val id: Long, val isSecret:Boolean): Event()
        data class UpdateIsMark(val id: Long, val isMark:Boolean): Event()
        data class UpdateTagList(val id:Long, val   selectTagList:ArrayList<Int>, val snippets:String): Event()
        data class ToRoute(val navController: NavController, val route:String) : Event()
    }


}