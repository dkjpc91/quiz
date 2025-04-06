package com.mithilakshar.learnsource.Utility

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.view.View
import android.widget.ScrollView
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView

class ViewShareUtil {

    companion object {
        fun viewToBitmap(view: View): Bitmap {
            val width = view.width
            var height = view.height

            if (view is NestedScrollView) {
                // Measure the total height of the ScrollView content
                height = 0
                for (i in 0 until view.childCount) {
                    height += view.getChildAt(i).height
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Fill the canvas with a white background
            canvas.drawColor(android.graphics.Color.WHITE)

            // Draw the view onto the canvas
            view.draw(canvas)
            return bitmap
        }

        fun shareViewAsImageDirectly(view: View, context: Context) {
            val bitmap = viewToBitmap(view)
            val contentUri = Uri.parse("content://${context.packageName}.bitmapprovider")
            BitmapContentProvider.setBitmap(contentUri, bitmap)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                val shareText = "ऐप: \n\n@mithilakshar13"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "ऐप"))
        }
    }
}
