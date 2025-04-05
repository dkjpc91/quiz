package com.mithilakshar.learnsource.Utility

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File


class dbHelper(context: Context, dbName: String) {

    private val TAG = "DBHelper"
    val dbFolderPath = context.getExternalFilesDir(null)?.absolutePath + File.separator + "test"
    val dbFilePath = "$dbFolderPath/$dbName"
    private var db: SQLiteDatabase? = null

    init {
        try {
            db = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READWRITE)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening database", e)
        }
    }









    @SuppressLint("Range")
    fun getRowsByCategoryname(categoryInput: String): List<Map<String, Any?>> {
        val matchedRows = mutableListOf<Map<String, Any?>>()

        db?.let { database ->
            if (!database.isOpen) {
                Log.w(TAG, "Database not open for reading rows by category")
                return emptyList()
            }

            val query = "SELECT * FROM LearnSourceMasterFile WHERE category = ? COLLATE NOCASE"
            val columnNames = listOf(
                "sno", "category", "subcategory", "name",
                "description", "image", "sourceurl", "audiourl",
                "videourl", "codename"
            )

            database.rawQuery(query, arrayOf(categoryInput))?.use { cursor ->
                while (cursor.moveToNext()) {
                    val rowData = mutableMapOf<String, Any?>()
                    for (columnName in columnNames) {
                        val value = cursor.getString(cursor.getColumnIndex(columnName))
                        rowData[columnName] = value
                    }
                    matchedRows.add(rowData)
                }
            }
        }

        return matchedRows
    }


    @SuppressLint("Range")
    fun getAllRowsFromMasterFile(): List<Map<String, Any?>> {
        val allRows = mutableListOf<Map<String, Any?>>()

        db?.let { database ->
            if (!database.isOpen) {
                Log.w(TAG, "Database not open for reading all rows")
                return emptyList()
            }

            val query = "SELECT * FROM PExamsMasterFile"
            val columnNames = listOf(
                "sno", "category", "subcategory", "name",
                "description", "image", "sourceurl", "audiourl",
                "videourl", "codename"
            )

            database.rawQuery(query, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val rowData = mutableMapOf<String, Any?>()
                    for (columnName in columnNames) {
                        val value = cursor.getString(cursor.getColumnIndex(columnName))
                        rowData[columnName] = value
                    }
                    allRows.add(rowData)
                }
            }
        }

        return allRows
    }









}