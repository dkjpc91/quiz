package com.mithilakshar.learnsource.Utility

import android.content.Context
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

class UrlDownloader(private val context: Context) {

    private var downloadDirectory: File = File(context.getExternalFilesDir(null), "test")

    init {
        if (!downloadDirectory.exists()) {
            downloadDirectory.mkdirs()
        }
    }

    private var currentDownloadFile: File? = null
    private var downloadCallback: ((progress: Int, totalBytes: Long) -> Unit)? = null
    private var completionCallback: ((success: Boolean, file: File?) -> Unit)? = null

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Downloads a file from the given URL and saves it with the provided filename.
     */
    fun downloadFile(url: String, filename: String, progressCallback: (progress: Int, totalBytes: Long) -> Unit, completionCallback: (success: Boolean, file: File?) -> Unit) {
        this.downloadCallback = progressCallback
        this.completionCallback = completionCallback

        val request = Request.Builder().url(url).build()
        val newDownloadFile = File(downloadDirectory, filename)

        // Delete the file if it exists (for re-downloads)
        if (newDownloadFile.exists()) {
            newDownloadFile.delete()
        }

        currentDownloadFile = newDownloadFile

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                completionCallback(false, null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    completionCallback(false, null)
                    return
                }

                response.body?.let { body ->
                    val totalBytes = body.contentLength()
                    var downloadedBytes: Long = 0
                    val buffer = ByteArray(2048)

                    var inputStream: InputStream? = null
                    var fos: FileOutputStream? = null

                    try {
                        inputStream = body.byteStream()
                        fos = FileOutputStream(newDownloadFile)

                        var read: Int
                        while (inputStream.read(buffer).also { read = it } != -1) {
                            fos.write(buffer, 0, read)
                            downloadedBytes += read

                            // Report progress
                            val progress = (downloadedBytes * 100 / totalBytes).toInt()
                            downloadCallback?.invoke(progress, totalBytes)
                        }

                        fos.flush()
                        completionCallback(true, newDownloadFile)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        completionCallback(false, null)
                    } finally {
                        fos?.close()
                        inputStream?.close()
                    }
                }
            }
        })
    }

    /**
     * Deletes the currently downloaded file and reports status.
     */
    fun deleteFile(filename: String, deletionCallback: (success: Boolean, message: String) -> Unit) {
        val fileToDelete = File(downloadDirectory, filename)
        if (fileToDelete.exists()) {
            val deleted = fileToDelete.delete()
            currentDownloadFile = null
            if (deleted) {
                deletionCallback(true, "File deleted: ${fileToDelete.path}")
            } else {
                deletionCallback(false, "Failed to delete file: ${fileToDelete.path}")
            }
        } else {
            deletionCallback(false, "File not found: ${fileToDelete.path}")
        }
    }

    /**
     * Redownloads the file (deletes the old one if it exists).
     */
    fun redownloadFile(url: String, filename: String, progressCallback: (progress: Int, totalBytes: Long) -> Unit, completionCallback: (success: Boolean, file: File?) -> Unit) {
        deleteFile(filename) { success, message ->
            // Log deletion status
            println(message)
            // Proceed with downloading if deletion was successful
            if (success) {
                downloadFile(url, filename, progressCallback, completionCallback)
            } else {
                completionCallback(false, null)
            }
        }
    }
}
