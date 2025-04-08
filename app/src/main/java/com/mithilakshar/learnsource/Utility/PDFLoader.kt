

import android.content.Context
import android.view.View
import android.widget.TextView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle

import java.io.File

class PDFLoader(private val context: Context,) {

    companion object {
        private const val PDF_FOLDER_NAME = "test"
    }


    fun loadPdf(pdfView: PDFView, fileName: String,textView: TextView) {
        val pdfFile = getPdfFile(fileName)

        if (!pdfFile.exists()) {
            pdfView.visibility = View.GONE
            return
        }


        pdfView.fromFile(pdfFile).apply {
            enableSwipe(true) // Allow swiping
            swipeHorizontal(false) // true = horizontal, false = vertical (choose based on your UX)
            enableDoubletap(true)
            defaultPage(0)
            enableAnnotationRendering(true)
            password(null)
            enableAntialiasing(true)
            spacing(0)

            // ✅ Show a scroll handle for quick navigation
            scrollHandle(DefaultScrollHandle(context)) // Or your fragment context

            onPageChange { page, pageCount ->
                textView.text = "Page ${page + 1} of $pageCount"
            }
            load()
        }
    }

    private fun getPdfFile(fileName: String): File {
        val folder = File(context.getExternalFilesDir(null), PDF_FOLDER_NAME)
        if (!folder.exists()) folder.mkdirs()
        return File(folder, "$fileName.pdf")
    }
}
