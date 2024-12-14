package com.unchil.gismemocompose.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.unchil.gismemocompose.db.entity.MEMO_FILE_TBL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow


@Dao
interface MemoFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(it: List<MEMO_FILE_TBL>)

    @Query("SELECT * FROM MEMO_FILE_TBL WHERE id = :id ")
    fun select_Flow(id:Long): Flow<List<MEMO_FILE_TBL>>


    @Query("SELECT * FROM MEMO_FILE_TBL WHERE id = :id ")
    fun memoFileListFlow(id:Long): Flow<List<MEMO_FILE_TBL>>

    @Query("SELECT * FROM MEMO_FILE_TBL WHERE id =:id")
    fun select(id:Long): List<MEMO_FILE_TBL>

    @Query("DELETE FROM MEMO_FILE_TBL WHERE id = :id")
    suspend fun delete(id:Long)


    @Query("DELETE FROM MEMO_FILE_TBL")
    suspend fun trancate()

}