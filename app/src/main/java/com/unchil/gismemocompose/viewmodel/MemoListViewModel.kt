package com.unchil.gismemocompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.unchil.gismemocompose.data.Repository
import com.unchil.gismemocompose.db.entity.MEMO_TBL
import com.unchil.gismemocompose.model.SearchQueryData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MemoListViewModel(val repository:Repository, ) : ViewModel() {


    private val _isRefreshingStateFlow: MutableStateFlow<Boolean>
            = MutableStateFlow(false)

    val isRefreshingStateFlow: StateFlow<Boolean>
            = _isRefreshingStateFlow.asStateFlow()

    var memoPagingStream :Flow<PagingData<MEMO_TBL>>

    private val searchQueryFlow:Flow<Event.Search>

    val eventHandler: (Event) -> Unit

    init {
        val eventStateFlow = MutableSharedFlow<Event>()

        //  1. fun onEvent(event: Event)
        eventHandler = {
            viewModelScope.launch {
                eventStateFlow.emit(it)
            }
        }

        //  2.   when (event) { is Event.Search ->   }
        searchQueryFlow = eventStateFlow
            .filterIsInstance<Event.Search>()
            .distinctUntilChanged()
            .onStart {
                emit(Event.Search(queryData = SearchQueryData))
            }

        //  3.  viewModelScope.launch { searchMemo() }
        memoPagingStream = searchQueryFlow
            .flatMapLatest {
                searchMemo(queryData = it.queryData)
            }.cachedIn(viewModelScope)

        /*
cachedIn(viewModelScope)
A common use case for this caching is to cache PagingData in a ViewModel.
This can ensure that, upon configuration change (e.g. rotation),
then new Activity will receive the existing data immediately
rather than fetching it from scratch.
 */

    }


    fun onEvent(event: Event) {
        when (event) {
            is Event.ToRoute -> {
                toRoute(event.navController, event.route)
            }
            is Event.DeleteItem -> deleteItem(event.id)
            is Event.Search -> {
                searchMemoRefresh(queryData = event.queryData)
            }



            is Event.SetFiles -> setFiles(event.id)
            is Event.SetMemo -> setMemo(event.id)

            else -> {}
        }
    }

    private fun searchMemo(queryData: SearchQueryData): Flow<PagingData<MEMO_TBL>> {
        _isRefreshingStateFlow.value = true
        val result = repository.getMemoListStream(queryData = SearchQueryData)
        _isRefreshingStateFlow.value = false
        return result
    }

    private fun searchMemoRefresh(queryData: SearchQueryData) {
        _isRefreshingStateFlow.value = true
        memoPagingStream = repository.getMemoListStream(queryData = SearchQueryData)
        _isRefreshingStateFlow.value = false

    }


    private fun deleteItem(id:Long){
        viewModelScope.launch {
            repository.deleteMemo(id= id)

        }
    }

    private fun toRoute(navController: NavController, route:String){
        navController.navigate(route = route)
    }





    private fun setMemo(id:Long){
        viewModelScope.launch {
            repository.setMemo(id = id)
        }
    }

    private fun setFiles(id:Long){
        viewModelScope.launch {
            repository.setFiles(id = id)
        }
    }



    sealed class Event {

        data class ToRoute(val navController: NavController, val route:String) : Event()
        data class DeleteItem(val id:Long): Event()
        data class Search(val queryData:SearchQueryData) : Event()




        data class  SetMemo(val id: Long): Event()
        data class  SetFiles(val id: Long): Event()

    }








}

