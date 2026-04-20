package com.example.distridulce.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Wraps [FileProvider.getUriForFile] so the rest of the app never has to hard-code
 * the authority string or remember the FileProvider contract.
 *
 * Usage:
 * ```kotlin
 * val file = FacturaPdfRepository.downloadPdf(context, facturaId)
 * val uri  = PdfUriHelper.getUriForFile(context, file)
 * // uri is now safe to pass in an Intent or PrintDocumentAdapter
 * ```
 */
object PdfUriHelper {

    /**
     * Returns a `content://` [Uri] for [file] that external apps can read.
     *
     * The authority must match the one declared in AndroidManifest.xml
     * (`${applicationId}.fileprovider`) and the file must be inside one of
     * the paths declared in `res/xml/file_paths.xml`.
     *
     * @throws IllegalArgumentException if [file] is not covered by any declared path.
     */
    fun getUriForFile(context: Context, file: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
}
