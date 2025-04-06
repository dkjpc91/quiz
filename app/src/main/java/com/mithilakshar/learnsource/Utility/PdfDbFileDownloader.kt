package com.mithilakshar.learnsource.Utility

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream

class PdfDbFileDownloader(
    private val context: Context,
    private val callback: DownloadCallback
) {
    private val client = OkHttpClient()
    private var downloadJob: Job? = null
    private var isCancelled = false

    interface DownloadCallback {
        fun onDownloadStarted(fileType: String)
        fun onProgress(fileType: String, progress: Int)
        fun onFileDownloaded(fileType: String, file: File)
        fun onAllDownloadsComplete()
        fun onError(fileType: String, error: String)
        fun onDownloadCancelled()
    }

    // Now accepts a list of Pair(url, fileType, fileName)
    fun downloadFilesSequentially(downloads: List<Triple<String, String, String>>) {
        isCancelled = false
        downloadJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                for ((url, fileType, fileName) in downloads) {
                    if (isCancelled) break

                    withContext(Dispatchers.Main) {
                        callback.onDownloadStarted(fileType)
                    }

                    val result = downloadSingleFile(url, fileName, fileType)

                    if (!isCancelled) {
                        withContext(Dispatchers.Main) {
                            callback.onFileDownloaded(fileType, result)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (isCancelled) {
                        callback.onDownloadCancelled()
                    } else {
                        callback.onAllDownloadsComplete()
                    }
                }
            } catch (e: Exception) {
                if (!isCancelled) {
                    withContext(Dispatchers.Main) {
                        callback.onError("", "Download failed: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    private fun getFileNameFromUrl(url: String): String? {
        return try {
            url.substringAfterLast('/').takeIf { it.isNotBlank() }?.substringBefore('?')
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun downloadSingleFile(
        url: String,
        fileName: String,
        fileType: String
    ): File = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}")
        }

        val folderPath = context.getExternalFilesDir(null)?.absolutePath +
                File.separator + "test" // Changed to "test" folder
        val folder = File(folderPath).apply {
            if (!exists()) mkdirs()
            Log.d("Downloader", "Folder path: $absolutePath")
        }

        // Save the file with the provided fileName and fileType
        val outputFile = File(folder, "$fileName.$fileType")

        saveFileWithProgress(response, outputFile, fileType)
        outputFile
    }

    private suspend fun saveFileWithProgress(
        response: Response,
        outputFile: File,
        fileType: String
    ) {
        val body = response.body?.byteStream()
        val contentLength = response.body?.contentLength() ?: -1L
        var bytesCopied = 0L
        val buffer = ByteArray(8 * 1024)

        FileOutputStream(outputFile).use { output ->
            body?.use { input ->
                var bytes = input.read(buffer)
                while (bytes >= 0 && !isCancelled) {
                    output.write(buffer, 0, bytes)
                    bytesCopied += bytes
                    bytes = input.read(buffer)

                    if (contentLength > 0) {
                        val progress = (bytesCopied * 100 / contentLength).toInt()
                        withContext(Dispatchers.Main) {
                            callback.onProgress(fileType, progress)
                        }
                    }
                }
            }
        }

        if (isCancelled) {
            outputFile.delete() // Clean up cancelled download
            Log.d("Downloader", "Cancelled download deleted: ${outputFile.name}")
        } else {
            Log.d("Downloader", "File saved: ${outputFile.absolutePath}")
        }
    }

    fun cancelDownloads() {
        isCancelled = true
        downloadJob?.cancel()
        Log.d("Downloader", "Downloads cancelled by user")
    }
}
