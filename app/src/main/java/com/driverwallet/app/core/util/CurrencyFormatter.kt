package com.driverwallet.app.core.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val formatter = ThreadLocal.withInitial {
        NumberFormat.getNumberInstance(Locale("id", "ID"))
    }

    fun format(amount: Long): String = formatter.get()!!.format(amount)

    fun formatWithPrefix(amount: Long): String = "Rp ${format(amount)}"

    fun formatShort(amount: Long): String = when {
        amount >= 1_000_000_000 -> "${format(amount / 1_000_000_000)}M"
        amount >= 1_000_000 -> "${format(amount / 1_000_000)}jt"
        amount >= 1_000 -> "${format(amount / 1_000)}rb"
        else -> format(amount)
    }
}
