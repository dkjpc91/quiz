package com.mithilakshar.learnsource.Utility
import android.content.Context

import android.util.Log
import androidx.lifecycle.LiveData

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class FirebaseFileDownloader(private val context: Context) {


    private val TAG = "FirebaseFileDownloader"
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    val downloadProgressLiveData: MutableLiveData<Int> = MutableLiveData()





    fun retrieveURL(documentPath: String,action: String,
                    urlFieldName: String,
                    callback: (File?) -> Unit,
                    progressCallback: (Int) -> Unit)
    {
        // Retrieve the URL from Firestore
        firestore.document(documentPath)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val url = documentSnapshot.getString("test")
                    if (url != null) {
                        // Create a directory for storing downloaded files
                        val downloadDirectory = File(context.getExternalFilesDir(null), "test")
                        if (!downloadDirectory.exists()) {
                            downloadDirectory.mkdirs()
                        }

                        // Create a local file path
                        val localFile = File(downloadDirectory, urlFieldName)

                        if (localFile.exists()) {
                            if (action == "return") {
                                // File already exists locally, return it
                                downloadProgressLiveData.postValue(100)
                                callback(localFile)
                            } else if (action == "delete") {
                                // Delete the file and then download it
                                localFile.delete()
                                downloadFile(url, localFile, callback,progressCallback)
                            }
                            //

                        } else {

                            // File does not exist locally
                            if (action == "return" || action == "delete") {
                                // Download from Firebase Storage
                                downloadFile(url, localFile, callback,progressCallback)
                            }
                            // File does not exist locally, download from Firebase Storage

                        }
                    } else {
                        Log.d(TAG, "URL field is null")
                        callback(null)
                    }
                } else {
                    Log.d(TAG, "Document does not exist")
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error retrieving document", e)
                callback(null)
            }
    }


    private fun downloadFile(url: String, localFile: File, callback: (File?) -> Unit,progressCallback: (Int) -> Unit) {
        val storageRef = storage.getReferenceFromUrl(url)
        val downloadTask = storageRef.getFile(localFile)

        downloadTask.addOnSuccessListener {
            callback(localFile)
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error downloading file", e)
            callback(null)
        }.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            progressCallback(progress) // Send progress updates
            downloadProgressLiveData.postValue(progress)
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Download complete")
                // Perform another task only if the file download is completed
                performAnotherTask()
            }
        }
    }


    private fun performAnotherTask() {
        // Placeholder for additional tasks after download completion
        Log.d(TAG, "Performing another task after download")
    }




}