package com.mithilakshar.mithilapanchang.Utility

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.downloadPublicTo
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SupabaseFileDownloader(private val context: Context) {

    private val TAG = "SupabaseFileDownloader"
    private val supabaseUrl = com.mithilakshar.learnsource.BuildConfig.sUrl
    private val supabaseKey = com.mithilakshar.learnsource.BuildConfig.sK
    private val firestore = FirebaseFirestore.getInstance()

    private val bucketName = "sqldb" // Change to your bucket name

    fun retrieveURL(
        storagePath: String,
        action: String,
        urlFieldName: String,
        callback: (File?) -> Unit,
        progressCallback: (Int) -> Unit
    ) {
        Log.d(TAG, "Retrieving URL from Firestore at path: $storagePath")

        // Retrieve the URL from Firestore
        firestore.document(storagePath)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                Log.i(TAG, "Document retrieved successfully: ${documentSnapshot.id}")

                if (documentSnapshot.exists()) {
                    val url = documentSnapshot.getString("test")
                    if (url != null) {
                        Log.d(TAG, "URL retrieved: $url")
                        // Create a directory for storing downloaded files
                        val downloadDirectory = File(context.getExternalFilesDir(null), "test")
                        if (!downloadDirectory.exists()) {
                            downloadDirectory.mkdirs()
                            Log.i(TAG, "Download directory created: ${downloadDirectory.absolutePath}")
                        }

                        // Create a local file path
                        val localFile = File(downloadDirectory, urlFieldName)

                        if (localFile.exists()) {
                            Log.i(TAG, "Local file already exists: ${localFile.absolutePath}")
                            if (action == "return") {
                                // File already exists locally, return it
                                downloadProgressLiveData.postValue(100)
                                callback(localFile)
                            } else if (action == "delete") {
                                // Delete the file and then download it
                                localFile.delete()
                                Log.i(TAG, "Local file deleted: ${localFile.absolutePath}")
                                downloadFile(url, localFile, callback, progressCallback)
                            }
                        } else {
                            Log.i(TAG, "Local file does not exist, downloading...")
                            // File does not exist locally
                            if (action == "return" || action == "delete") {
                                // Download from Firebase Storage
                                downloadFile(url, localFile, callback, progressCallback)
                            }
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

    // Supabase client and storage initialization
    private val supabase = createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey
    ) {
        install(Storage) {
            // settings if needed
        }
    }

    private val bucket = supabase.storage.from(bucketName)
    val downloadProgressLiveData: MutableLiveData<Int> = MutableLiveData()

    /**
     * Downloads a file from Supabase Storage.
     * @param fileName The name of the file in Supabase Storage.
     * @param localFile The local file where the downloaded content will be saved.
     * @param callback Callback for the result file.
     * @param progressCallback Callback for download progress updates.
     */
    private fun downloadFile(
        fileName: String,
        localFile: File,
        callback: (File?) -> Unit,
        progressCallback: (Int) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting download for file: $fileName")
                // Download the file directly to the specified local file
                bucket.downloadPublicTo(fileName, localFile)

                // If successful, update progress and callback
                downloadProgressLiveData.postValue(100) // Download is complete
                Log.i(TAG, "Download completed successfully: ${localFile.absolutePath}")
                callback(localFile)
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading file: ${e.message}", e)
                callback(null)
            }
        }
    }
}
