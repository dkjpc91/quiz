package com.mithilakshar.learnsource.Utility



import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings




import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MyApplication: Application() {


    private lateinit var analytics: FirebaseAnalytics




    override fun onCreate() {
        super.onCreate()

        analytics = Firebase.analytics


    }

}