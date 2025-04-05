package com.mithilakshar.mithilapanchang.Dialog


import android.content.Context
import android.app.AlertDialog
import android.view.LayoutInflater
import com.mithilakshar.learnsource.R


class Networkdialog (context: Context): AlertDialog(context) {


        init {

            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.networkdialog, null)
            setView(view)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }