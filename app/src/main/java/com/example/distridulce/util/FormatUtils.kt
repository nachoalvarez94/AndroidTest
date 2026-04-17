package com.example.distridulce.util

import java.util.Locale

// ── Currency ──────────────────────────────────────────────────────────────────

private val esLocale = Locale("es", "ES")

/**
 * Formats a monetary amount as euros using the standard display format.
 * Example: 1234.5 → "€ 1234,50"
 *
 * Uses Spanish locale so decimal separator is a comma, consistent with
 * the rest of the app's locale-aware formatting.
 */
fun formatEur(amount: Double): String =
    String.format(esLocale, "€ %.2f", amount)

/**
 * Same as [formatEur] but without the leading space, suitable for inline
 * use inside sentences or compact table cells.
 */
fun formatEurCompact(amount: Double): String =
    String.format(esLocale, "€%.2f", amount)
