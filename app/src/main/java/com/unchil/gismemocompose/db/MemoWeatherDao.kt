package com.unchil.gismemocompose.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.unchil.gismemocompose.db.entity.MEMO_WEATHER_TBL
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoWeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(it:MEMO_WEATHER_TBL)

    @Query("DELETE FROM MEMO_WEATHER_TBL")
    suspend fun trancate()

    @Query("DELETE FROM MEMO_WEATHER_TBL WHERE id = :id")
    fun delete(id:Long)

    @Query("SELECT * FROM MEMO_WEATHER_TBL WHERE ID = :id  LIMIT 1 ")
    fun select_Flow(id:Long): Flow<MEMO_WEATHER_TBL>


}