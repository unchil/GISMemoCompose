package com.unchil.gismemocompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unchil.gismemocompose.data.Repository
import com.unchil.gismemocompose.model.MemoDataUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MemoContainerViewModel (
    val repository: Repository,
    val user: MemoDataUser
) : ViewModel() {

    private val _photoListStateFlow: MutableStateFlow<List<String>>
            = MutableStateFlow(emptyList())

    val photoListStateFlow: StateFlow<List<String>>
            = _photoListStateFlow.asStateFlow()


    private val _videoListStateFlow: MutableStateFlow<List<String>>
            = MutableStateFlow(emptyList())

    val videoListStateFlow:  StateFlow<List<String>>
            = _videoListStateFlow.asStateFlow()

    private val _audioTextStateFlow: MutableStateFlow<List<Pair<String, List<String>>>>
            = MutableStateFlow(emptyList())

    val audioTextStateFlow: StateFlow<List<Pair<String, List<String>>>>
            = _audioTextStateFlow.asStateFlow()


    private val  _snapShotListStateFlow: MutableStateFlow<List<String>>
            = MutableStateFlow(emptyList())

    val snapShotListStateFlow:  StateFlow<List<String>>
            = _snapShotListStateFlow.asStateFlow()


    init {
        when(user){
            MemoDataUser.DetailMemoView -> {

                viewModelScope.launch {
                    repository.detailVideo.collectLatest { it ->
                        _videoListStateFlow.value = it
                    }
                }

                viewModelScope.launch {
                    repository.detailAudioText.collectLatest { it ->
                        _audioTextStateFlow.value = it
                    }
                }

                viewModelScope.launch {
                    repository.detailPhoto.collectLatest { it ->
                        _photoListStateFlow.value = it
                    }
                }

                viewModelScope.launch {
                    repository.detailSnapShot.collectLatest { it ->
                        _snapShotListStateFlow.value = it
                    }
                }
            }
            MemoDataUser.WriteMemoView -> {

                viewModelScope.launch {
                    repository.currentVideo.collectLatest { it ->
                        _videoListStateFlow.value = it
                    }
                }

                viewModelScope.launch {
                    repository.currentAudioText.collectLatest { it ->
                        _audioTextStateFlow.value = it
                    }
                }

                viewModelScope.launch {
                    repository.currentPhoto.collectLatest { it ->
                        _photoListStateFlow.value = it
                    }
                }

                viewModelScope.launch {
                    repository.currentSnapShot.collectLatest { it ->
                        _snapShotListStateFlow.value = it
                    }
                }




            }
        }
    }

}