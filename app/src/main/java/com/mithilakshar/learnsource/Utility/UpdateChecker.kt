package com.mithilakshar.learnsource.Utility

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.mithilakshar.learnsource.Room.UpdatesDao

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UpdateChecker(private val updatesDao: UpdatesDao) {

    private val db = FirebaseFirestore.getInstance()
    private val collectionRef = db.collection("masterupdate")
    private val documentRef = collectionRef.document("masterupdate")

    var actions =""

    suspend fun getUpdateStatus(): String {
        return withContext(Dispatchers.IO) {
            try {
                val document = documentRef.get().await()
                if (document != null) {
                    actions = document.getString("updatecode") ?: "delete"
                    val updates = updatesDao.getfileupdate("masterupdate.db")
                    Log.d("updatechecker", " :  UPDATE  $updates")
                    Log.d("updatechecker", " : action $actions")
                    if (updates.isNotEmpty() && updates[0].uniqueString == actions) {
                        // No update required, return "a"
                        "a"
                    } else {
                        // Update required, return the action string
                        val masterupdate = updatesDao.findById(99)
                        masterupdate?.let {
                            it.uniqueString = actions
                            updatesDao.update(it)
                        }
                        actions
                    }
                } else {
                    // Document not found, assume update is required
                    actions
                }
            } catch (e: Exception) {
                Log.e("UpdateChecker", "Error checking update", e)
                // Return the action string or "a" depending on the context
                "a"
            }
        }
    }
}