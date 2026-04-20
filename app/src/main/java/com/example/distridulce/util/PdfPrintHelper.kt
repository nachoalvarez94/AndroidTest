package com.example.distridulce.util

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import java.io.File
import java.io.FileOutputStream

/**
 * Sends a local PDF file to the Android print framework.
 *
 * Usage:
 * ```kotlin
 * val file = FacturaPdfRepository.downloadPdf(context, facturaId)
 * PdfPrintHelper.print(context, file, "FAC-00042")
 * ```
 */
object PdfPrintHelper {

    /**
     * Opens the system print dialog for [file].
     *
     * The [PrintManager] handles printer selection, page range, copies, etc.
     * [FilePrintDocumentAdapter] streams the raw PDF bytes to the print spooler —
     * the framework renders each page on its own.
     *
     * @param context  Activity context (required by [PrintManager]).
     * @param file     Local PDF file, e.g. the one returned by [FacturaPdfRepository.downloadPdf].
     * @param jobTitle Shown in the print queue as the document name.
     */
    fun print(context: Context, file: File, jobTitle: String = "Factura DistriDulce") {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val adapter      = FilePrintDocumentAdapter(file, jobTitle)
        printManager.print(jobTitle, adapter, PrintAttributes.Builder().build())
    }
}

// ── PrintDocumentAdapter ──────────────────────────────────────────────────────

/**
 * Minimal [PrintDocumentAdapter] that streams a pre-rendered PDF file directly
 * to the Android print spooler.
 *
 * This works when the backend already generates a proper PDF with embedded page
 * definitions.  The framework reads the page dimensions from the PDF itself, so
 * [onLayout] only needs to declare the document type — no manual page-size
 * computation is required.
 */
private class FilePrintDocumentAdapter(
    private val file: File,
    private val title: String
) : PrintDocumentAdapter() {

    /**
     * Called when print attributes change (e.g. user picks a different paper size).
     * We always report the same document metadata regardless of attributes because
     * the PDF already defines its own page layout.
     */
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder(title)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            // PAGE_COUNT_UNKNOWN is fine — the framework reads the actual count from the PDF.
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()

        // Pass `true` as the second argument only when the layout actually changed;
        // using `newAttributes != oldAttributes` avoids redundant re-renders.
        callback.onLayoutFinished(info, newAttributes != oldAttributes)
    }

    /**
     * Called when the framework is ready to receive the PDF bytes.
     * Streams [file] directly into [destination] — no intermediate buffer.
     */
    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onWriteCancelled()
            return
        }

        try {
            file.inputStream().use { input ->
                FileOutputStream(destination.fileDescriptor).use { output ->
                    input.copyTo(output)
                }
            }
            // Signal that all requested pages have been written.
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback.onWriteFailed(e.message)
        }
    }
}
