package com.example.distridulce.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Launches the system share sheet for a PDF file exposed as a [content://] [Uri].
 *
 * The Uri must come from [PdfUriHelper.getUriForFile] so the FileProvider
 * grants read permission to the receiving app automatically.
 *
 * Usage:
 * ```kotlin
 * val file = FacturaPdfRepository.downloadPdf(context, facturaId)
 * val uri  = PdfUriHelper.getUriForFile(context, file)
 * PdfShareHelper.share(context, uri, "FAC-00042")
 * ```
 */
object PdfShareHelper {

    /**
     * Opens the Android chooser with all apps capable of receiving a PDF.
     *
     * [FLAG_GRANT_READ_URI_PERMISSION] is added to the chooser Intent so the
     * selected app can read the content:// Uri without needing storage permissions.
     *
     * @param context  Activity or application context.
     * @param uri      content:// Uri obtained from [PdfUriHelper.getUriForFile].
     * @param title    Shown in the chooser header and used as the email/message subject.
     */
    fun share(context: Context, uri: Uri, title: String = "Compartir factura PDF") {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            // Let the target app read our FileProvider uri.
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Wrap in a chooser so the user can pick any compatible app.
        val chooser = Intent.createChooser(shareIntent, title).apply {
            // Propagate the permission grant through the chooser intent as well.
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(chooser)
    }
}
