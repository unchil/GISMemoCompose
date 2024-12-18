package com.unchil.gismemocompose.data

import androidx.compose.runtime.compositionLocalOf
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.unchil.gismemocompose.BuildConfig
import com.unchil.gismemocompose.api.GisMemoApi
import com.unchil.gismemocompose.db.CURRENTWEATHER_TBL
import com.unchil.gismemocompose.db.LuckMemoDB
import com.unchil.gismemocompose.db.entity.CURRENTLOCATION_TBL
import com.unchil.gismemocompose.db.entity.MEMO_FILE_TBL
import com.unchil.gismemocompose.db.entity.MEMO_TAG_TBL
import com.unchil.gismemocompose.db.entity.MEMO_TBL
import com.unchil.gismemocompose.db.entity.MEMO_TEXT_TBL
import com.unchil.gismemocompose.db.entity.MEMO_WEATHER_TBL
import com.unchil.gismemocompose.db.entity.toMEMO_WEATHER_TBL
import com.unchil.gismemocompose.model.SearchQueryData
import com.unchil.gismemocompose.model.WriteMemoData
import com.unchil.gismemocompose.model.toCURRENTWEATHER_TBL
import com.unchil.gismemocompose.view.DrawingPolyline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


val LocalRepository = compositionLocalOf<Repository> { error("Not Found Handler Repository") }


class Repository{

    private val api = GisMemoApi()
    lateinit var database:LuckMemoDB


    val currentSelectedTab:MutableStateFlow<WriteMemoData.Type?>
        = MutableStateFlow(null)

    val selectedMemo:MutableStateFlow<MEMO_TBL?> = MutableStateFlow(null)

    val selectedWeather:MutableStateFlow<MEMO_WEATHER_TBL?> = MutableStateFlow(null)

    val isChangeLocale: MutableStateFlow<Int>
            = MutableStateFlow(0)

    // locale 실시간 반영을 위한 state
    val realTimeChangeLocale: MutableStateFlow<Boolean>
            = MutableStateFlow(false)


    val isFirstSetup:MutableStateFlow<Boolean>
            = MutableStateFlow(true)

    val isUsableHaptic: MutableStateFlow<Boolean>
            = MutableStateFlow(true)

    val isUsableDarkMode: MutableStateFlow<Boolean>
            = MutableStateFlow(false)

    val isUsableDynamicColor: MutableStateFlow<Boolean>
            = MutableStateFlow(false)

    val currentIsDrawing: MutableStateFlow<Boolean>
            = MutableStateFlow(false)

    val currentIsEraser: MutableStateFlow<Boolean>
            = MutableStateFlow(false)

    var selectedTagList: MutableStateFlow<ArrayList<Int>>
        = MutableStateFlow(arrayListOf())

    val currentIsLock: MutableStateFlow<Boolean>
            = MutableStateFlow(false)

    val currentIsMarker: MutableStateFlow<Boolean>
        = MutableStateFlow(false)


    val currentPolylineList:MutableStateFlow<List<DrawingPolyline>>
        = MutableStateFlow(listOf())

    fun clearCurrentValue(){
        selectedTagList.value = arrayListOf()

        currentIsLock.value = false
        currentIsMarker.value = false
        currentIsDrawing.value = false
        currentIsEraser.value = false
        currentPolylineList.value = listOf()
        currentSnapShot.value = listOf()
    }

    fun updateIsFirstSetup(value:Boolean){
        isFirstSetup.value = value
    }

    fun updateRealTimeChangeLocale(value:Boolean){
        realTimeChangeLocale.value = value
    }

    fun updateIsUsableHaptic(value:Boolean){
        isUsableHaptic.value = value
    }

    fun updateIsUsableDarkMode(value:Boolean){
        isUsableDarkMode.value = value
    }

    fun updateIsUsableDynamicColor(value:Boolean){
        isUsableDynamicColor.value = value
    }



    fun updateIsChangeLocale(value:Int){
        isChangeLocale.value = value
    }


    fun updateCurrentIsDrawing(isDrawing:Boolean){
        currentIsDrawing.value = isDrawing
    }


    fun updateCurrentIsEraser(isEraser:Boolean){
        currentIsEraser.value = isEraser
    }


    fun updateCurrentTags(tagArrayList:ArrayList<Int>){
        selectedTagList.value = tagArrayList
    }

    fun updateCurrentIsLock(isLock:Boolean){
        currentIsLock.value = isLock
    }

    fun updateCurrentIsMarker(isMarker:Boolean){
        currentIsMarker.value = isMarker
    }



    fun updateCurrentPolylineList(polylineList:List<DrawingPolyline>){
        currentPolylineList.value = polylineList
    }

//------
    val currentAudioText: MutableStateFlow<List<Pair<String, List<String>>>>
            = MutableStateFlow( listOf())

    fun setAudioText(audioTextList: List<Pair<String, List<String>>>){
        currentAudioText.value = audioTextList
    }

    fun setPhotoVideo(photoList:List<String>, videoList:List<String>){
        currentPhoto.value = photoList
        currentVideo.value = videoList
    }

    fun setSnapShot(data:List<String> ){
        currentSnapShot.value = data
    }

    fun setSelectedTab(data: WriteMemoData.Type){
        currentSelectedTab.value = data
    }


    val currentPhoto:  MutableStateFlow<List<String>>
    = MutableStateFlow( listOf())

    val currentVideo: MutableStateFlow<List<String>>
            = MutableStateFlow( listOf())

    val currentSnapShot: MutableStateFlow<List<String>>
            = MutableStateFlow( listOf())

    val detailAudioText: MutableStateFlow<List<Pair<String, List<String>>>>
            = MutableStateFlow( listOf())

    val detailPhoto:  MutableStateFlow<List<String>>
            = MutableStateFlow( listOf())

    val detailVideo: MutableStateFlow<List<String>>
            = MutableStateFlow( listOf())

    val detailSnapShot: MutableStateFlow<List<String>>
            = MutableStateFlow( listOf())



//------

    val OPENWEATHER_URL = "https://api.openweathermap.org/data/2.5/"

    val _currentWeather:MutableStateFlow<CURRENTWEATHER_TBL?>  = MutableStateFlow(null)

    suspend fun deleteMemoItem( type:WriteMemoData.Type,  index:Int) {
        when(type){
            WriteMemoData.Type.PHOTO -> {
                val newMemoItem = currentPhoto.value.toMutableList()
                newMemoItem.removeAt(index)
                currentPhoto.emit(newMemoItem)
            }
            WriteMemoData.Type.AUDIOTEXT -> {
                val newMemoItem = currentAudioText.value.toMutableList()
                newMemoItem.removeAt(index)
                currentAudioText.emit(newMemoItem)
            }
            WriteMemoData.Type.VIDEO -> {
                val newMemoItem = currentVideo.value.toMutableList()
                newMemoItem.removeAt(index)
                currentVideo.emit(newMemoItem)
            }
            WriteMemoData.Type.SNAPSHOT -> {
                val newMemoItem = currentSnapShot.value.toMutableList()
                newMemoItem.removeAt(index)
                currentSnapShot.emit(newMemoItem)
            }
        }
    }



    suspend fun initMemoItem(){

        WriteMemoData.Types.forEach {

            when(it){
                WriteMemoData.Type.PHOTO -> {
                    val newMemoItem = currentPhoto.value.toMutableList()
                    newMemoItem.clear()
                    currentPhoto.emit(newMemoItem)
                }

                WriteMemoData.Type.AUDIOTEXT -> {
                    val newMemoItem = currentAudioText.value.toMutableList()
                    newMemoItem.clear()
                    currentAudioText.emit(newMemoItem)
                }
                WriteMemoData.Type.VIDEO -> {
                    val newMemoItem = currentVideo.value.toMutableList()
                    newMemoItem.clear()
                    currentVideo.emit(newMemoItem)
                }
                WriteMemoData.Type.SNAPSHOT -> {
                    val newMemoItem = currentSnapShot.value.toMutableList()
                    newMemoItem.clear()
                    currentSnapShot.emit(newMemoItem)
                }

            }
        }

    }


    suspend fun insertMemo (
        id:Long,
        isLock:Boolean,
        isMark:Boolean,
        selectTagArrayList:ArrayList<Int>,
        title:String,
        desc:String,
        snippets: String,
        location: CURRENTLOCATION_TBL
    )  {


        val memoFileTblList = mutableListOf<MEMO_FILE_TBL>()
        val memoTextTblList = mutableListOf<MEMO_TEXT_TBL>()
/*
        val desc =
            "screenshot:${currentSnapShot.value.size} audioText:${currentAudioText.value.size} photo:${currentPhoto.value.size} video:${currentVideo.value.size}"

 */

        val snapshot = currentSnapShot.value.first()
        val memoTbl = MEMO_TBL(
            id = id,
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            isSecret = isLock,
            isPin = isMark,
            title = title,
            snippets = snippets,
            snapshotCnt = currentSnapShot.value.size,
            textCnt = currentAudioText.value.size,
            photoCnt = currentPhoto.value.size,
            videoCnt = currentVideo.value.size,
            desc = desc,
            snapshot = snapshot
        )


        database.memoDao.insert(memoTbl)

        currentSnapShot.value.forEachIndexed { index, uri ->
            memoFileTblList.add(
                MEMO_FILE_TBL(
                    id = id,
                    type = WriteMemoData.Type.SNAPSHOT.name,
                    index = index,
                    subIndex = 0,
                    filePath = uri
                )
            )
        }

        currentPhoto.value.forEachIndexed { index, uri ->
            memoFileTblList.add(
                MEMO_FILE_TBL(
                    id = id,
                    type = WriteMemoData.Type.PHOTO.name,
                    index = index,
                    subIndex = 0,
                    filePath = uri
                )
            )
        }

        currentVideo.value.forEachIndexed { index, uri ->
            memoFileTblList.add(
                MEMO_FILE_TBL(
                    id = id,
                    type = WriteMemoData.Type.VIDEO.name,
                    index = index,
                    subIndex = 0,
                    filePath = uri
                )
            )
        }


        currentAudioText.value.forEachIndexed { index, pairData ->
            memoTextTblList.add(
                MEMO_TEXT_TBL(
                    id = id,
                    index = index,
                    comment = pairData.first
                )
            )

            pairData.second.forEachIndexed { subIndex, uri ->
                memoFileTblList.add(
                    MEMO_FILE_TBL(
                        id = id,
                        type = WriteMemoData.Type.AUDIOTEXT.name,
                        index = index,
                        subIndex = subIndex,
                        filePath = uri
                    )
                )
            }
        }


        val memoTagTblList = mutableListOf(MEMO_TAG_TBL(id = id, index = 10000))
        selectTagArrayList.forEach {
            memoTagTblList.add(MEMO_TAG_TBL(id = id, index = it))
        }

         database.memoTagDao.insert(memoTagTblList)
         database.memoFileDao.insert(memoFileTblList)
         database.memoTextDao.insert(memoTextTblList)
        _currentWeather.value?.toMEMO_WEATHER_TBL(id)?.let {
            database.memoWeatherDao.insert(it)
        }



        initMemoItem()
    }


    suspend fun deleteMemo(id:Long){
        database.withTransaction {
            database.memoDao.delete(id)
            database.memoFileDao.delete(id)
            database.memoTextDao.delete(id)
            database.memoTagDao.delete(id)
            database.memoWeatherDao.delete(id)
        }
    }

    suspend fun deleteAllMemo(){
        database.memoDao.trancate()
    }


    val _markerMemoList:MutableStateFlow<List<MEMO_TBL>>  = MutableStateFlow(listOf())

    suspend fun setMarkerMemoList() {

        database.memoDao.select_Marker_Flow().collectLatest {
            _markerMemoList.emit(it)
        }

    }


      fun getShareMemoData(id:Long, completeHandle:(attachments:ArrayList<String>, comments:ArrayList<String>)->Unit )
        = CoroutineScope(Dispatchers.IO).launch {
            val attachments = arrayListOf<String>()
            val comments = arrayListOf<String>()

          database.memoFileDao.select(id).forEach {
              attachments.add(it.filePath)
          }
          database.memoTextDao.select(id).forEach {
              comments.add(it.comment)
          }
            completeHandle(attachments, comments)
        }



     fun setMemo(id:Long){
        CoroutineScope(Dispatchers.IO).launch {
            selectedMemo.value = database.memoDao.select(id)
        }
    }


     fun setTags(id:Long){
        CoroutineScope(Dispatchers.IO).launch {
            val tagArrayList = arrayListOf<Int>()
            database.memoTagDao.select(id).forEach {
                tagArrayList.add(it.index)
            }
            selectedTagList.value = tagArrayList
        }
    }



    suspend fun setWeather(id:Long){
        database.memoWeatherDao.select_Flow(id).collectLatest {
           selectedWeather.value = it
        }
    }

    suspend fun setFiles(id:Long){


        database.memoFileDao.memoFileListFlow(id).collectLatest { it ->
            val currentSnapShotList = it.filter {
                it.type ==  WriteMemoData.Type.SNAPSHOT.name
            }.sortedBy {
                it.index
            }.map {
                it.filePath
            }

            detailSnapShot.emit( currentSnapShotList )

            val currentPhotoList = it.filter {
                it.type ==  WriteMemoData.Type.PHOTO.name
            }.sortedBy {
                it.index
            }.map {
                it.filePath
            }

            detailPhoto.emit(  currentPhotoList )

            val currentVideoList = it.filter {
                it.type == WriteMemoData.Type.VIDEO.name
            }.sortedBy {
                it.index
            }.map {
                it.filePath
            }

            detailVideo.emit(  currentVideoList  )

            database.memoTextDao.memoTextListFlow(id).collectLatest {memoTextTblList ->

                val audiTextList = mutableListOf<Pair<String,List<String>>>()
                val audioTextFileList = it.filter { it.type ==  WriteMemoData.Type.AUDIOTEXT.name}

                memoTextTblList.forEach {commentList ->
                    audiTextList.add(
                        Pair(
                            commentList.comment,
                            audioTextFileList.filter {
                                it.index == commentList.index
                            }.sortedBy {
                                it.subIndex
                            }.map {
                                it.filePath
                            }
                        )
                    )
                }
                detailAudioText.value = audiTextList
            }

        }


    }



    suspend fun updateTagList(id:Long, selectTagList: ArrayList< Int>, snippets:String){


       val memoTagTblList = mutableListOf(MEMO_TAG_TBL(id = id, index = 10000))
       selectTagList.forEach {
           memoTagTblList.add(MEMO_TAG_TBL(id = id, index = it))
       }



        database.withTransaction {

            database.memoDao.update_Snippets(id, snippets)
            database.memoTagDao.delete(id)
            database.memoTagDao.insert(memoTagTblList)

        }
    }


    suspend fun updateMark(id:Long, isMark:Boolean){
        database.memoDao.update_Marker(id, isMark)
    }

    suspend fun updateSecret(id:Long, isSecret:Boolean){
        database.memoDao.update_Secret(id, isSecret)
    }



    suspend fun getWeatherData(latitude: String, longitude: String){

        val OPENWEATHER_KEY = BuildConfig.OPENWEATHER_KEY
        val OPENWEATHER_UNITS = "metric"


        try {
            val apiResponse = api.getWeatherData(
                lat = latitude,
                lon = longitude,
                units = OPENWEATHER_UNITS,
                appid = OPENWEATHER_KEY
            )


            database.withTransaction {
                database.currentWeatherDao.delete()
                database.currentWeatherDao.insert(apiResponse.toCURRENTWEATHER_TBL())
            }

            _currentWeather.emit(apiResponse.toCURRENTWEATHER_TBL())

        } catch(e:Exception){
            val message = e.localizedMessage
        }


    }


    fun getMemoListStream(queryData: SearchQueryData): Flow<PagingData<MEMO_TBL>> {

       return  if(SearchQueryData.value.isEmpty() ){
           Pager(
               config = PagingConfig(
                   pageSize = 30,
                   enablePlaceholders = false
               ),
               pagingSourceFactory = {
                   database.memoDao.select_All_Paging()
               }
           ).flow
        } else {
           var title = "% %"
           var tagArray = arrayListOf(10000)
           var secretArray  = arrayListOf(0,1)
           var markerArray = arrayListOf(0,1)
           var fromDate = 0L
           var toDate = System.currentTimeMillis()

           SearchQueryData.Types.forEach {
               when(it){
                   SearchQueryData.Type.TITLE -> {
                       if (SearchQueryData.value.containsKey(SearchQueryData.Type.TITLE)) {
                           val tempTitle = SearchQueryData.value[SearchQueryData.Type.TITLE].toString()
                           title = "%" + tempTitle.replace(' ','%' )  + "%"
                       }
                   }
                   SearchQueryData.Type.SECRET -> {
                       if (SearchQueryData.value.containsKey(SearchQueryData.Type.SECRET)) {
                           val secretIndex = SearchQueryData.value[SearchQueryData.Type.SECRET] as Long
                           secretArray =  if(secretIndex == 0L) arrayListOf(1) else arrayListOf(0)
                       }
                   }
                   SearchQueryData.Type.MARKER -> {
                       if ( SearchQueryData.value.containsKey(SearchQueryData.Type.MARKER)){
                           val markerIndex = SearchQueryData.value[SearchQueryData.Type.MARKER] as Long
                           markerArray = if(markerIndex == 0L) arrayListOf(1) else arrayListOf(0)
                       }

                   }
                   SearchQueryData.Type.TAG -> {
                       if (SearchQueryData.value.containsKey(SearchQueryData.Type.TAG)) {
                           tagArray = SearchQueryData.value[SearchQueryData.Type.TAG] as ArrayList<Int>
                       }
                   }
                   SearchQueryData.Type.DATE -> {
                       if ( SearchQueryData.value.containsKey(SearchQueryData.Type.DATE)) {
                           val tempDate = SearchQueryData.value[SearchQueryData.Type.DATE] as Pair<Long, Long>
                           fromDate = tempDate.first
                           toDate = tempDate.second
                       }
                   }
               }
           }

            Pager(
               config = PagingConfig(
                   pageSize = 30,
                   enablePlaceholders = false
               ),
               pagingSourceFactory = {
                   database.memoDao.select_Search_Paging(
                       tagArray,
                       fromDate,
                       toDate,
                       secretArray,
                       markerArray,
                       title
                   )
               }
           ).flow

        }

    }




}




