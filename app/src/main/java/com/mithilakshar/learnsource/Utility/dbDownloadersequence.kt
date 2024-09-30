package com.mithilakshar.learnsource.Utility

import android.app.Activity
import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.mithilakshar.learnsource.Room.Updates
import com.mithilakshar.learnsource.Room.UpdatesDao

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class dbDownloadersequence(
    private val updatesDao: UpdatesDao,
    private val firebaseFileDownloader: FirebaseFileDownloader
) {

    fun observeMultipleFileExistence(
        filesWithIds: List<Pair<String, Int>>,
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        homeActivity: Activity,
        progressCallback: (Int, String) -> Unit,
        onComplete: () -> Unit
    ) {
        val totalFiles = filesWithIds.size
        var filesProcessed = 0

        fun downloadNextFile(index: Int) {
            if (index >= filesWithIds.size) {
                // All files processed
                Log.d("dbd", "All files processed successfully")
                onComplete() // Call onComplete callback here
                return
            }

            val (filename, updatedaoid) = filesWithIds[index]
            Log.d("dbd", "Processing file: $filename with update ID: $updatedaoid")

            val fileExistenceLiveData = checkFileExistence("$filename.db", homeActivity)
            val db = FirebaseFirestore.getInstance()
            val collectionRef = db.collection("SQLdb")
            val documentRef = collectionRef.document(filename)

            fileExistenceLiveData.observe(lifecycleOwner) { fileExists ->
                Log.d("dbd", "File existence check result for $filename: $fileExists")
                if (fileExists) {
                    Log.d("dbd", "File exists: $filename")
                    documentRef.get().addOnSuccessListener { documentSnapshot ->
                        Log.d("dbd", "Document data: ${documentSnapshot?.data}")
                        if (documentSnapshot != null) {
                            val actions = documentSnapshot.getString("action") ?: "delete"
                            coroutineScope.launch {
                                try {
                                    val existingUpdate = withContext(Dispatchers.IO) {
                                        updatesDao.getfileupdate("$filename.db")
                                    }
                                    Log.d("dbd", "Current updates for $filename: $existingUpdate")
                                    if (existingUpdate.isNotEmpty() && existingUpdate[0].uniqueString == actions) {
                                        Log.d("dbd", "File already up to date: $filename")
                                        progressCallback(100, filename)
                                        filesProcessed++
                                        downloadNextFile(index + 1)
                                    } else {
                                        Log.d("dbd", "File out of date or not found, downloading new version: $filename")
                                        val holidayUpdate = withContext(Dispatchers.IO) {
                                            updatesDao.findById(updatedaoid)
                                        }
                                        if (holidayUpdate != null) {
                                            holidayUpdate.uniqueString = actions
                                            withContext(Dispatchers.IO) {
                                                updatesDao.update(holidayUpdate)
                                            }
                                            Log.d("dbd", "Updated existing record for: $filename")
                                        } else {
                                            val newUpdate = Updates(
                                                id = updatedaoid,
                                                fileName = "$filename.db",
                                                uniqueString = actions
                                            )
                                            withContext(Dispatchers.IO) {
                                                updatesDao.insert(newUpdate)
                                            }
                                            Log.d("dbd", "Inserted new record for: $filename")
                                        }

                                        val storagePath = "SQLdb/$filename"
                                        downloadFile(storagePath, "delete", "$filename.db", progressCallback) {
                                            // After the download completes and the database has been updated, proceed to the next file
                                            filesProcessed++
                                            downloadNextFile(index + 1)
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("dbd", "Error during database operation: ", e)
                                    filesProcessed++
                                    downloadNextFile(index + 1)
                                }
                            }
                        } else {
                            Log.d("dbd", "No document found for: $filename")
                            filesProcessed++
                            downloadNextFile(index + 1)
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("dbd", "Error fetching document: ", exception)
                        filesProcessed++
                        downloadNextFile(index + 1)
                    }
                } else {
                    Log.d("dbd", "File does not exist: $filename")
                    val storagePath = "SQLdb/$filename"
                    downloadFile(storagePath, "delete", "$filename.db", progressCallback) {
                        documentRef.get().addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot != null) {
                                val actions = documentSnapshot.getString("action") ?: "delete"
                                val newUpdate = Updates(
                                    id = updatedaoid,
                                    fileName = "$filename.db",
                                    uniqueString = actions
                                )
                                coroutineScope.launch {
                                    try {
                                        withContext(Dispatchers.IO) {
                                            updatesDao.insert(newUpdate)
                                        }
                                        Log.d("dbd", "Inserted new record for: $filename")
                                    } catch (e: Exception) {
                                        Log.e("dbd", "Error inserting record for $filename: ", e)
                                    }
                                }
                            }
                            filesProcessed++
                            downloadNextFile(index + 1)
                        }.addOnFailureListener { exception ->
                            Log.e("dbd", "Error fetching document: ", exception)
                            filesProcessed++
                            downloadNextFile(index + 1)
                        }
                    }
                }
            }
        }

        downloadNextFile(0) // Start processing files
    }

    private fun downloadFile(
        storagePath: String,
        action: String,
        localFileName: String,
        progressCallback: (Int, String) -> Unit,
        onComplete: () -> Unit
    ) {
        firebaseFileDownloader.retrieveURL(storagePath, action, localFileName, { downloadedFile ->
            Log.d(ContentValues.TAG, "Downloaded file path: $downloadedFile")
            onComplete() // Notify that the download is complete
        }, { progress ->
            Log.d(ContentValues.TAG, "Download progress: $progress")
            progressCallback(progress, localFileName)
        })
    }

    fun checkFileExistence(fileName: String, homeActivity: Activity): LiveData<Boolean> {
        val fileExistsLiveData = MutableLiveData<Boolean>()
        val dbFolderPath = homeActivity.getExternalFilesDir(null)?.absolutePath + File.separator + "test"
        val dbFile = File(dbFolderPath, fileName)
        fileExistsLiveData.value = dbFile.exists()
        return fileExistsLiveData
    }
}