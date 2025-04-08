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
                "videourl", "codename","notesurl","quiztype"
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
                "videourl", "codename","notesurl","quiztype"
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

    @SuppressLint("Range")
    fun getAllRowsFromTable(tableName: String): List<Map<String, Any?>> {
        val allRows = mutableListOf<Map<String, Any?>>()

        db?.let { database ->
            if (!database.isOpen) {
                Log.w(TAG, "Database not open for reading all rows")
                return emptyList()
            }

            // Validate table name to prevent SQL injection
            if (!tableName.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                Log.e(TAG, "Invalid table name: $tableName")
                return emptyList()
            }

            try {
                // First get all column names for the table
                val columnNames = mutableListOf<String>()
                database.rawQuery("PRAGMA table_info($tableName)", null)?.use { cursor ->
                    while (cursor.moveToNext()) {
                        columnNames.add(cursor.getString(cursor.getColumnIndex("name")))
                    }
                }

                if (columnNames.isEmpty()) {
                    Log.w(TAG, "No columns found for table: $tableName")
                    return emptyList()
                }

                // Now fetch all rows
                val query = "SELECT * FROM $tableName"
                database.rawQuery(query, null)?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val rowData = mutableMapOf<String, Any?>()
                        for (columnName in columnNames) {
                            try {
                                when (cursor.getType(cursor.getColumnIndex(columnName))) {
                                    Cursor.FIELD_TYPE_STRING ->
                                        rowData[columnName] = cursor.getString(cursor.getColumnIndex(columnName))
                                    Cursor.FIELD_TYPE_INTEGER ->
                                        rowData[columnName] = cursor.getInt(cursor.getColumnIndex(columnName))
                                    Cursor.FIELD_TYPE_FLOAT ->
                                        rowData[columnName] = cursor.getFloat(cursor.getColumnIndex(columnName))
                                    Cursor.FIELD_TYPE_BLOB ->
                                        rowData[columnName] = cursor.getBlob(cursor.getColumnIndex(columnName))
                                    Cursor.FIELD_TYPE_NULL ->
                                        rowData[columnName] = null
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Error reading column $columnName: ${e.message}")
                                rowData[columnName] = null
                            }
                        }
                        allRows.add(rowData)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading from table $tableName: ${e.message}")
            }
        }

        return allRows
    }



    @SuppressLint("Range")
    fun quizdbdata(tableName: String): List<Map<String, Any?>> {
        val maxRows = 100
        val allRows = mutableListOf<Map<String, Any?>>()

        db?.let { database ->
            if (!database.isOpen) {
                Log.w(TAG, "Database not open for reading from $tableName")
                return emptyList()
            }

            val query = "SELECT * FROM $tableName LIMIT $maxRows"

            database.rawQuery(query, null)?.use { cursor ->
                val columnNames = cursor.columnNames
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


    @SuppressLint("Range")
    fun getRandomRowFromMasterFile(table: String?): Map<String, Any?>? {
        if (table.isNullOrBlank()) {
            Log.w(TAG, "Table name is null or blank")
            return null
        }

        db?.let { database ->
            if (!database.isOpen) {
                Log.w(TAG, "Database not open for reading random row")
                return null
            }

            val query = "SELECT * FROM $table ORDER BY RANDOM() LIMIT 1"
            val columnNames = listOf(
                "sno", "category", "subcategory", "name",
                "description", "image", "sourceurl", "audiourl",
                "videourl", "codename", "notesurl", "quiztype"
            )

            try {
                database.rawQuery(query, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        return columnNames.associateWith { columnName ->
                            cursor.getString(cursor.getColumnIndexOrThrow(columnName))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading random row from $table: ${e.message}")
            }
        }

        return null
    }




}