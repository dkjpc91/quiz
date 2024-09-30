package com.mithilakshar.learnsource.Room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface  UpdatesDao  {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(Updates: Updates)
    @Update
    suspend fun update(Updates: Updates)

    @Query("SELECT * FROM files")
    suspend fun getallupdates(): List<Updates>

    @Query("SELECT * FROM files WHERE id = :id")
    suspend fun findById(id: kotlin.Int): Updates

    @Query("SELECT * FROM files WHERE fileName = :fileName")
    suspend fun getfileupdate(fileName: String): List<Updates>

    @Query("UPDATE files SET uniqueString = :newUniqueString WHERE fileName = :fileName")
    suspend fun updateUniqueString(fileName: String, newUniqueString: String)
}