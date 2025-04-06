

import android.content.Context
import android.view.View
import com.github.barteksc.pdfviewer.PDFView

import java.io.File

class PDFLoader(private val context: Context) {

    companion object {
        private const val PDF_FOLDER_NAME = "test"
    }

    fun loadPdf(pdfView: PDFView, fileName: String) {
        val pdfFile = getPdfFile(fileName)

        if (!pdfFile.exists()) {
            pdfView.visibility = View.GONE
            return
        }

        pdfView.fromFile(pdfFile).apply {
            enableSwipe(true)
            enableDoubletap(true)
            defaultPage(0)
            enableAnnotationRendering(false)
            password(null)
            enableAntialiasing(true)
            spacing(0)
            load()
        }
    }

    private fun getPdfFile(fileName: String): File {
        val folder = File(context.getExternalFilesDir(null), PDF_FOLDER_NAME)
        if (!folder.exists()) folder.mkdirs()
        return File(folder, "$fileName.pdf")
    }
}
