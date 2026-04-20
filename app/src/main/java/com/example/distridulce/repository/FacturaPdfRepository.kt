package com.example.distridulce.repository

import android.content.Context
import com.example.distridulce.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Handles downloading invoice PDFs from the backend and caching them locally.
 *
 * Files are stored in [Context.cacheDir]/facturas/factura_{id}.pdf.
 * The OS may evict the cache at any time, so always check [File.exists] before
 * serving a cached file and re-download if needed.
 *
 * Kept separate from [FacturaRepository] so PDF-specific dependencies
 * (Context, streaming I/O) do not pollute the JSON-only repository.
 */
object FacturaPdfRepository {

    private val api = RetrofitClient.facturasApi

    /**
     * Downloads the PDF for [facturaId] and saves it to the app's cache directory.
     *
     * - Runs entirely on [Dispatchers.IO] (network + disk).
     * - If the file already exists in cache it is **overwritten** to ensure freshness.
     * - Throws [IllegalStateException] if the server returns a non-2xx response or an
     *   empty body.
     * - Throws [java.io.IOException] on any I/O failure during the write.
     *
     * @return The saved [File] ready for use with [PdfUriHelper.getUriForFile].
     */
    suspend fun downloadPdf(context: Context, facturaId: Long): File =
        withContext(Dispatchers.IO) {
            val response = api.downloadFacturaPdf(facturaId)

            if (!response.isSuccessful) {
                error("Error al descargar el PDF: HTTP ${response.code()} ${response.message()}")
            }

            val body = response.body()
                ?: error("El servidor devolvió una respuesta vacía para la factura $facturaId")

            // Ensure the destination directory exists.
            val facturaDir = File(context.cacheDir, "facturas")
            facturaDir.mkdirs()

            val pdfFile = File(facturaDir, "factura_$facturaId.pdf")

            // Stream directly to disk — no intermediate in-memory buffer.
            body.byteStream().use { input ->
                pdfFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            pdfFile
        }

    /**
     * Returns the cached PDF file for [facturaId] if it already exists,
     * or null if it has not been downloaded yet (or was evicted by the OS).
     */
    fun getCachedPdf(context: Context, facturaId: Long): File? {
        val file = File(context.cacheDir, "facturas/factura_$facturaId.pdf")
        return file.takeIf { it.exists() }
    }
}
