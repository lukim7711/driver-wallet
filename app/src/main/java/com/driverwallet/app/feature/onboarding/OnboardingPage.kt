package com.driverwallet.app.feature.onboarding

import androidx.compose.runtime.Immutable

@Immutable
data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
)

val onboardingPages = listOf(
    OnboardingPage(
        emoji = "\uD83C\uDFE0",
        title = "Dashboard Keuangan",
        description = "Pantau pemasukan, pengeluaran, dan sisa budget harian kamu dalam satu layar. Semua ringkas dan jelas.",
    ),
    OnboardingPage(
        emoji = "\u26A1",
        title = "Catat Cepat",
        description = "Satu tap untuk catat transaksi. Pilih kategori, masukkan nominal, selesai! Dirancang untuk driver yang sibuk.",
    ),
    OnboardingPage(
        emoji = "\uD83D\uDCB3",
        title = "Kelola Hutang",
        description = "Catat cicilan dan hutang. Lihat progress pelunasan dan target tanggal lunas kamu.",
    ),
    OnboardingPage(
        emoji = "\uD83D\uDCCA",
        title = "Laporan Lengkap",
        description = "Lihat laporan mingguan, bulanan, atau rentang custom. Export ke CSV untuk arsip pribadi kamu.",
    ),
)
