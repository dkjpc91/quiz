package com.mithilakshar.learnsource.Room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class Updates(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val fileName: String,
    var uniqueString: String
)