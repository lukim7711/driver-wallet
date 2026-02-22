# AD_02: User Stories & Flows — Driver Wallet Android

> **Audience:** AI Builder & Developer
> **Konvensi:** US-XX = User Story, AC-XX.Y = Acceptance Criteria, UF-XX = User Flow
> **Referensi:** Semua flow merujuk ke Compose screens, ViewModels, dan Room DAOs
> **Prerequisite:** Baca [AD_01_PRD_Product.md](./AD_01_PRD_Product.md) terlebih dahulu

---

## Daftar User Stories

| ID | Judul | Feature Ref | Priority |
|----|-------|-------------|----------|
| US-01 | Lihat Ringkasan Keuangan Hari Ini | F01 Dashboard | P0 (Must) |
| US-02 | Catat Transaksi Cepat (Quick-Tap) | F02 Quick Input | P0 (Must) |
| US-03 | Kelola Daftar Hutang | F05 Debt Management | P0 (Must) |
| US-04 | Bayar Cicilan Hutang | F05 Debt Management | P0 (Must) |
| US-05 | Lihat Laporan Mingguan | F06 Report | P1 (Should) |
| US-06 | Lihat Laporan Bulanan | F06 Report | P1 (Should) |
| US-07 | Lihat Laporan Custom Range | F06 Report | P2 (Nice) |
| US-08 | Export Laporan ke CSV | F06 Report | P2 (Nice) |
| US-09 | Atur Budget Harian | F07 Settings | P0 (Must) |
| US-10 | Atur Pengeluaran Tetap | F07 Settings | P1 (Should) |
| US-11 | Atur Target Tanggal Lunas | F07 Settings | P1 (Should) |
| US-12 | Onboarding Pertama Kali | F08 Onboarding | P2 (Nice) |
| US-13 | Navigasi Antar Screen | F09 Bottom Nav | P0 (Must) |

---

## US-01: Lihat Ringkasan Keuangan Hari Ini

> **Sebagai** driver ojol,
> **saya ingin** membuka app dan langsung melihat pemasukan, pengeluaran, dan keuntungan hari ini,
> **agar** saya tahu apakah hari ini sudah untung atau belum.

### Acceptance Criteria

| ID | Kriteria | Detail |
|----|----------|--------|
| AC-01.1 | Hero Card Keuntungan | `DashboardScreen` menampilkan card besar berisi: **profit** = totalIncome − totalExpense − totalDebtPayment. Font besar (~42sp). Warna `primaryContainer` |
| AC-01.2 | Badge Persentase | Menampilkan persentase perubahan profit vs hari kemarin. Format: "+8%" atau "-12%". Jika tidak ada data kemarin, sembunyikan badge |
| AC-01.3 | Income & Expense Cards | 2 card grid: total Masuk (arrow_downward, primary) dan total Keluar (arrow_upward, error). Nominal diformat singkat ("Rp 150rb") |
| AC-01.4 | Target Harian | Tampilkan progress bar: `earnedToday / targetAmount`. Badge "ON TRACK" jika progress ≥ (currentHour / 24), "OFF TRACK" jika tidak. Tampilkan "Rp X / Y" di samping persentase |
| AC-01.5 | Sisa Budget | Card menampilkan: budget terpakai + sisa budget hari ini. Progress bar `LinearProgressIndicator`. Budget = SUM(daily_budgets) dari Settings |
| AC-01.6 | Alert Cicilan | Card warning muncul jika ada debt_schedule dengan status UNPAID dan jatuh tempo ≤ 7 hari. Tampilkan: nama platform, "jatuh tempo dalam X hari". Sort by urgency (overdue first) |
| AC-01.7 | Transaksi Hari Ini | List max 5 transaksi terbaru hari ini. Setiap item: icon kategori (circle), nama kategori, waktu (HH:mm), dan nominal. Link "Lihat Semua" untuk expand. Jika 0 transaksi: "Belum ada transaksi hari ini" |
| AC-01.8 | Pull-to-Refresh | Swipe down memicu `DashboardViewModel` reload semua data dari Room |
| AC-01.9 | Onboarding | Pada first launch (DataStore `has_seen_onboarding == false`), tampilkan OnboardingOverlay di atas Dashboard |

### UF-01: Dashboard Flow

```
App Launch
  │
  ├─ [First Launch?] ─── YES ──→ OnboardingOverlay
  │                                   │
  │                              Tap "Mulai"
  │                                   │
  │                     DataStore.set(has_seen_onboarding = true)
  │                                   │
  │   ◄────────────────────────────────┘
  │
  ▼
DashboardScreen
  │
  ▼
DashboardViewModel.loadDashboard(today: LocalDate)
  │
  ├── GetDashboardSummaryUseCase.invoke(date)
  │     │
  │     ├── TransactionRepository.getTodaySummary(date)
  │     │     └── TransactionDao.getTodaySummary(date)
  │     │         → returns: SUM(income), SUM(expense), COUNT(*)
  │     │
  │     ├── TransactionRepository.getYesterdaySummary(date - 1)
  │     │     └── → untuk kalkulasi badge persentase
  │     │
  │     ├── DailyExpenseRepository.getActiveExpenses()
  │     │     └── DailyExpenseDao.getAll(isDeleted = false)
  │     │
  │     ├── MonthlyExpenseRepository.getActiveExpenses()
  │     │     └── MonthlyExpenseDao.getAll(isDeleted = false)
  │     │
  │     ├── DebtRepository.getActiveDebtsWithSchedule()
  │     │     └── DebtDao.getActiveWithNextSchedule()
  │     │         → returns: List<DebtWithSchedule>
  │     │
  │     └── SettingsRepository.get("debt_target_date")
  │           └── SettingsDao.getByKey(key)
  │
  ├── Emit UiState.Success(DashboardData)
  │
  ▼
Render:
  ├── HeroCard(profit, percentChange)
  ├── IncomeExpenseRow(income, expense)
  ├── DailyTargetSection(earned, target, isOnTrack)
  ├── BudgetRemainingCard(used, total)
  ├── DueAlertCard(nearestDueDebts)   // jika ada
  └── TodayTransactionList(transactions, max=5)

User Interactions:
  ├── Pull-to-refresh → re-invoke loadDashboard()
  ├── Tap "Lihat Semua" → expand transaction list (show all today)
  └── Tap BottomNav item → navigate to target screen
```

### UiState Definition

```kotlin
sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(
        val todaySummary: TodaySummary,     // income, expense, debtPayment, profit, count
        val percentChange: Int?,             // nullable jika tidak ada data kemarin
        val dailyTarget: DailyTarget,        // earned, target, isOnTrack, percentage
        val budgetRemaining: BudgetInfo,     // used, total, percentage
        val dueAlerts: List<DueAlert>,       // sorted by urgency
        val recentTransactions: List<TransactionItem>,
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}
```

---

## US-02: Catat Transaksi Cepat (Quick-Tap)

> **Sebagai** driver ojol yang sedang di jalan,
> **saya ingin** mencatat transaksi dalam maksimal 4 tap dan < 3 detik,
> **agar** saya bisa langsung lanjut mengemudi tanpa terganggu.

### Acceptance Criteria

| ID | Kriteria | Detail |
|----|----------|--------|
| AC-02.1 | Type Toggle | Segmented button: MASUK (default selected) / KELUAR. Mengubah tipe mengubah daftar kategori yang ditampilkan |
| AC-02.2 | Kategori Grid | Menampilkan grid kategori sesuai tipe. Income: 5 kategori (Order, Tips, Bonus, Insentif, Lainnya). Expense: 8 kategori. Default show 4, "Lihat Semua" untuk expand. Kategori aktif: highlight `primaryContainer` |
| AC-02.3 | Amount Display | Teks besar nominal dengan prefix "Rp". User mengetik via custom number pad ATAU menggunakan preset buttons |
| AC-02.4 | Quick Presets | 3 tombol cepat: +10rb, +20rb, +50rb. Tap menambahkan ke nominal (additif). Contoh: saat ini Rp 0 → tap +10rb → Rp 10.000 → tap +50rb → Rp 60.000 |
| AC-02.5 | Custom Number Pad | Grid 3×4: angka 1-9, ".", 0, backspace (⌫). Tap angka append ke display. Backspace hapus digit terakhir |
| AC-02.6 | Catatan Opsional | TextField "Tambah catatan...", maxLength 100 karakter. Opsional, boleh kosong |
| AC-02.7 | Validasi Simpan | Tombol SIMPAN disabled (alpha 38%) jika: amount == 0 ATAU kategori belum dipilih |
| AC-02.8 | Prevent Double Tap | Saat saving: label berubah "⏳ Menyimpan...", button disabled. Re-enable setelah success/error |
| AC-02.9 | Success Feedback | Setelah berhasil simpan: Snackbar "✅ Transaksi tersimpan" + navigate back ke Dashboard |
| AC-02.10 | Close Button | Tombol X (close) di kiri atas untuk cancel dan kembali ke screen sebelumnya tanpa simpan |

### UF-02: Quick-Tap Input Flow

```
BottomNav "Input" tap
  │
  ▼
QuickInputScreen
  │
  ▼
QuickInputViewModel → emit UiState.Ready(type=INCOME, categories, amount=0)
  │
  ▼
┌─────────────────────────────────────────────────┐
│  [X Close]        [MASUK ● | KELUAR]            │  ← Header
│                                                  │
│  Kategori          [Lihat Semua]                 │
│  [🛍 Order✓] [💰 Tips] [🎁 Bonus] [⋯ Lainnya] │  ← Grid
│                                                  │
│              Rp 15.000                           │  ← Amount
│                                                  │
│  [📝 Tambah catatan...]                         │  ← Note
│                                                  │
│  [+10rb]  [+20rb]  [+50rb]                      │  ← Presets
│                                                  │
│   [1] [2] [3]                                    │
│   [4] [5] [6]                                    │  ← Number Pad
│   [7] [8] [9]                                    │
│   [.] [0] [⌫]                                   │
│                                                  │
│  [████████ SIMPAN ████████]                      │  ← CTA
└─────────────────────────────────────────────────┘

User Actions:
  │
  ├── Tap MASUK/KELUAR → UiAction.ToggleType
  │     → ViewModel updates categories list + resets selection
  │
  ├── Tap kategori → UiAction.SelectCategory(id)
  │     → ViewModel updates selectedCategory
  │
  ├── Tap number pad / preset → UiAction.UpdateAmount(digit | preset)
  │     → ViewModel updates amount display
  │
  ├── Type catatan → UiAction.UpdateNote(text)
  │
  └── Tap SIMPAN → UiAction.Save
        │
        ▼
      ViewModel:
        ├── validate(amount > 0, category != null)
        ├── emit UiState.Saving
        ├── SaveTransactionUseCase.invoke(
        │     type, amount, category, note, source="manual"
        │   )
        │     └── TransactionRepository.insert(transaction)
        │           └── TransactionDao.insert(entity)
        ├── emit UiEvent.ShowSnackbar("Transaksi tersimpan")
        └── emit UiEvent.NavigateBack
```

### UiState, UiAction, UiEvent

```kotlin
// === UiState ===
sealed interface QuickInputUiState {
    data object Loading : QuickInputUiState
    data class Ready(
        val type: TransactionType,            // INCOME | EXPENSE
        val categories: List<CategoryItem>,   // berdasarkan type
        val selectedCategory: String?,        // category id
        val amount: Long,                     // dalam Rupiah (Int)
        val amountDisplay: String,            // formatted: "15.000"
        val note: String,                     // catatan, default ""
        val isSaveEnabled: Boolean,           // amount > 0 && category != null
        val isSaving: Boolean,                // true saat proses simpan
    ) : QuickInputUiState
}

// === UiAction (user intent) ===
sealed interface QuickInputUiAction {
    data class ToggleType(val type: TransactionType) : QuickInputUiAction
    data class SelectCategory(val categoryId: String) : QuickInputUiAction
    data class AppendDigit(val digit: Char) : QuickInputUiAction
    data object DeleteDigit : QuickInputUiAction
    data class AddPreset(val amount: Long) : QuickInputUiAction  // 10_000, 20_000, 50_000
    data class UpdateNote(val text: String) : QuickInputUiAction
    data object Save : QuickInputUiAction
}

// === UiEvent (one-shot) ===
sealed interface QuickInputUiEvent {
    data class ShowSnackbar(val message: String) : QuickInputUiEvent
    data object NavigateBack : QuickInputUiEvent
}
```

### Amount Formatting Rules

```
Input: user taps 1, 5, 0, 0, 0
Internal: amount = 15000 (Long)
Display: "15.000" (titik sebagai pemisah ribuan)
Prefix: "Rp" ditampilkan terpisah di sebelah kiri

Preset additif:
  amount = 0 → tap +10rb → amount = 10_000 → display "10.000"
  amount = 10_000 → tap +50rb → amount = 60_000 → display "60.000"

Backspace:
  amount = 15_000 (display "15.000") → backspace → amount = 1_500 (display "1.500")
  amount = 1 → backspace → amount = 0 (display "0")

Max amount: 999_999_999 (< 1 milyar, cukup untuk use case driver ojol)
```

---

## US-03: Kelola Daftar Hutang

> **Sebagai** driver ojol yang punya hutang di beberapa platform,
> **saya ingin** melihat daftar semua hutang, sisa masing-masing, dan progressnya,
> **agar** saya bisa memprioritaskan hutang mana yang harus dilunasi dulu.

### Acceptance Criteria

| ID | Kriteria | Detail |
|----|----------|--------|
| AC-03.1 | Hero Total | Card besar `bg-primary` menampilkan total sisa hutang (SUM semua remaining_amount). Warning badge jika ada jatuh tempo minggu ini |
| AC-03.2 | Debt Card List | List card per hutang. Setiap card: platform icon + nama, badge status ("Aktif" / "Lunas X%"), sisa hutang nominal, gradient progress bar, info cicilan "X dari Y", persentase lunas |
| AC-03.3 | Due Date Info | Per card: icon calendar + tanggal jatuh tempo berikutnya + info bunga (persentase) |
| AC-03.4 | Action Buttons | Per card: tombol "Detail" (outlined) + tombol "Bayar" (filled primary). "Bayar" disabled jika hutang sudah lunas |
| AC-03.5 | Tambah Hutang | Tombol `+` di header → navigate ke `DebtFormScreen` |
| AC-03.6 | Edit Hutang | Tap "Detail" → navigate ke `DebtFormScreen` pre-filled |
| AC-03.7 | Hapus Hutang | Swipe-to-dismiss atau menu di Detail → Dialog konfirmasi → soft delete |
| AC-03.8 | Empty State | Jika tidak ada hutang: tampilkan ilustrasi + "Belum ada hutang tercatat" + tombol "Tambah Hutang" |
| AC-03.9 | Riwayat | Tombol "Lihat Riwayat" → tampilkan hutang yang sudah lunas (semua cicilan paid) |

### UF-03: Debt List Flow

```
BottomNav "Hutang" tap
  │
  ▼
DebtListScreen
  │
  ▼
DebtListViewModel.loadDebts()
  │
  ├── GetActiveDebtsUseCase.invoke()
  │     └── DebtRepository.getActiveDebtsWithSchedule()
  │           └── DebtDao.getActiveWithNextSchedule()
  │               → returns List<DebtWithSchedule>
  │                   DebtWithSchedule {
  │                     debt: DebtEntity,
  │                     nextSchedule: DebtScheduleEntity?,
  │                     paidCount: Int,
  │                     totalCount: Int,
  │                     paidPercentage: Float
  │                   }
  │
  ├── Emit UiState.Success(totalRemaining, debtCards)
  │
  ▼
Render:
  ├── HeroTotalCard(totalRemaining, warningCount)
  ├── SectionHeader("Daftar Pinjaman", onRiwayatClick)
  └── LazyColumn {
        items(debtCards, key = { it.debt.id }) { card ->
          DebtCardItem(
            platformIcon, platformName, status,
            remainingAmount, progressPercent,
            installmentInfo, dueDate, interestRate,
            onDetailClick, onPayClick
          )
        }
      }

User Actions:
  ├── Tap "+" → navigate(DebtFormRoute(debtId = null))     // tambah
  ├── Tap "Detail" → navigate(DebtFormRoute(debtId = X))    // edit
  ├── Tap "Bayar" → showPaymentDialog(debtId = X)           // bayar
  └── Tap "Lihat Riwayat" → filter = COMPLETED
```

### DebtForm Screen — Tambah / Edit

```
DebtFormScreen(debtId: String?)
  │
  ▼
DebtFormViewModel.init(debtId)
  │
  ├── if debtId != null → load existing debt from Room → pre-fill form
  │
  ▼
Form Fields:
  │
  ├── platform: String (required)
  │     └── Dropdown/TextField: "Shopee Pinjam", "GoPay Later", "Kredivo", dll.
  │
  ├── totalAmount: Long (required)
  │     └── TextField numeric: Total pinjaman awal (Rp)
  │
  ├── installmentPerMonth: Long (required)
  │     └── TextField numeric: Nominal cicilan per bulan (Rp)
  │
  ├── installmentCount: Int (required)
  │     └── TextField numeric: Jumlah total cicilan (contoh: 6)
  │
  ├── dueDay: Int (required, 1-31)
  │     └── TextField/Picker: Tanggal jatuh tempo setiap bulan
  │
  ├── interestRate: Double (required, bisa 0)
  │     └── TextField decimal: Bunga per bulan (contoh: 2.5%)
  │
  ├── penaltyType: String
  │     └── Dropdown: "none" | "fixed" | "percentage"
  │
  ├── penaltyRate: Double (required if penaltyType != "none")
  │     └── TextField: Nominal/persentase denda
  │
  ├── debtType: String
  │     └── Dropdown: "pinjaman_tunai" | "paylater" | "cicilan_barang" | "lainnya"
  │
  └── note: String (optional, max 200)

On Save:
  ├── Validate all required fields
  ├── SaveDebtUseCase.invoke(debt)
  │     └── Room @Transaction {
  │           DebtDao.upsert(debtEntity)
  │           DebtScheduleDao.generateSchedules(debt)  // auto-generate X schedules
  │         }
  ├── Snackbar: "Hutang berhasil disimpan"
  └── Navigate back to DebtListScreen

Schedule Generation Logic:
  for (i in 1..installmentCount) {
    DebtSchedule(
      debtId = debt.id,
      installmentNumber = i,
      dueDate = startDate.plusMonths(i),
      amount = installmentPerMonth,
      status = "unpaid",     // "unpaid" | "paid" | "overdue"
      paidAt = null,
    )
  }
```

---

## US-04: Bayar Cicilan Hutang

> **Sebagai** driver ojol,
> **saya ingin** mencatat pembayaran cicilan hutang,
> **agar** progress pelunasan saya ter-update otomatis.

### Acceptance Criteria

| ID | Kriteria | Detail |
|----|----------|--------|
| AC-04.1 | Pay Dialog | Tap "Bayar" pada debt card → tampilkan BottomSheet/Dialog |
| AC-04.2 | Schedule Selection | Tampilkan jadwal cicilan UNPAID berikutnya. User bisa pilih jadwal mana yang mau dibayar |
| AC-04.3 | Amount Confirmation | Tampilkan nominal cicilan. User bisa edit nominal jika bayar lebih/kurang dari jadwal |
| AC-04.4 | Atomic Transaction | Saat konfirmasi bayar, **satu Room @Transaction** yang melakukan: (1) Update debt.remaining_amount, (2) Update schedule.status = "paid" + paidAt, (3) Insert Transaction(type="expense", category="cicilan", source="debt_payment", debtId=X) |
| AC-04.5 | Progress Update | Setelah bayar, debt card langsung update: progress bar, "Cicilan X dari Y", persentase, remaining amount |
| AC-04.6 | Lunas Detection | Jika semua schedule status = "paid" (atau remaining_amount ≤ 0): otomatis update badge ke "Lunas" dan fade card (opacity 0.75) |
| AC-04.7 | Success Feedback | Snackbar: "Cicilan [Platform] berhasil dibayar" |

### UF-04: Payment Flow

```
DebtListScreen → Tap "Bayar" pada card Shopee Pinjam
  │
  ▼
PaymentBottomSheet(debtId)
  │
  ▼
PaymentViewModel.loadNextSchedule(debtId)
  │
  ├── DebtScheduleDao.getNextUnpaid(debtId)
  │     → returns: DebtSchedule(installment=4, amount=416.667, dueDate=...)
  │
  ▼
┌─────────────────────────────────────────┐
│  Bayar Cicilan                          │
│                                         │
│  Shopee Pinjam                          │
│  Cicilan ke-4 dari 6                    │
│  Jatuh tempo: 25 Nov 2024              │
│                                         │
│  Nominal:                               │
│  ┌──────────────────────────────┐       │
│  │ Rp 416.667                   │       │
│  └──────────────────────────────┘       │
│                                         │
│  [Batal]              [✓ Bayar Sekarang]│
└─────────────────────────────────────────┘

Tap "Bayar Sekarang":
  │
  ▼
PaymentViewModel.confirmPayment(debtId, scheduleId, amount)
  │
  ▼
PayDebtInstallmentUseCase.invoke(debtId, scheduleId, amount)
  │
  └── Room @Transaction {
        // 1. Update debt remaining
        val debt = debtDao.getById(debtId)
        val newRemaining = debt.remainingAmount - amount
        debtDao.updateRemaining(debtId, newRemaining)

        // 2. Mark schedule as paid
        debtScheduleDao.markAsPaid(
          scheduleId = scheduleId,
          paidAt = nowJakarta().toIsoString(),
          actualAmount = amount
        )

        // 3. Insert as expense transaction
        transactionDao.insert(
          Transaction(
            type = "expense",
            amount = amount,
            category = "cicilan",
            note = "Cicilan ${debt.platform} ke-${schedule.number}",
            source = "debt_payment",
            debtId = debtId,
          )
        )

        // 4. Check if fully paid
        val unpaidCount = debtScheduleDao.countUnpaid(debtId)
        if (unpaidCount == 0 || newRemaining <= 0) {
          debtDao.updateStatus(debtId, "completed")
        }
      }
  │
  ▼
Dismiss BottomSheet
  │
  ├── Snackbar: "Cicilan Shopee Pinjam berhasil dibayar"
  └── DebtListViewModel auto-refresh (observing Room Flow)
```

---

## US-05: Lihat Laporan Mingguan

> **Sebagai** driver ojol,
> **saya ingin** melihat laporan pemasukan vs pengeluaran per hari dalam seminggu,
> **agar** saya tahu hari apa yang paling menguntungkan.

### Acceptance Criteria

| ID | Kriteria | Detail |
|----|----------|--------|
| AC-05.1 | Tab Default | Tab "Mingguan" aktif secara default saat buka ReportScreen |
| AC-05.2 | Week Navigation | Chevron kiri/kanan untuk navigasi antar minggu. Label: "Minggu Ini" / "Minggu Lalu" + date range badge |
| AC-05.3 | Bar Chart | Dual-bar chart: 7 hari (Sen–Min). Bar income (purple primary) + bar expense (light purple). Hari terpilih: highlight dengan shadow + bold label |
| AC-05.4 | Summary Cards | 2 card: Pemasukan total (Rp X) + Pengeluaran total (Rp Y) |
| AC-05.5 | Profit Card | Card besar: "Total Keuntungan" = Pemasukan − Pengeluaran |
| AC-05.6 | Daily Detail List | List per hari: tanggal (circle), nama hari, jumlah transaksi (badge), profit (+Rp X), breakdown In/Out |
| AC-05.7 | Tap Day Detail | Tap pada hari di list → expand/show transaksi detail hari tersebut |

### UF-05: Weekly Report Flow

```
BottomNav "Laporan" tap
  │
  ▼
ReportScreen(initialTab = WEEKLY)
  │
  ▼
ReportViewModel.loadWeeklyReport(weekOffset = 0)  // 0 = minggu ini
  │
  ├── GetWeeklyReportUseCase.invoke(startOfWeek, endOfWeek)
  │     └── TransactionRepository.getGroupedByDay(start, end)
  │           └── TransactionDao.getDailySummary(start, end)
  │               → returns List<DailySummary> {
  │                   date: LocalDate,
  │                   totalIncome: Long,
  │                   totalExpense: Long,
  │                   transactionCount: Int
  │               }
  │
  ├── Calculate:
  │     weekIncome = sum(daily.totalIncome)
  │     weekExpense = sum(daily.totalExpense)
  │     weekProfit = weekIncome - weekExpense
  │     barChartData = 7 items (Mon-Sun), fill 0 for missing days
  │
  ├── Emit UiState.WeeklyReport(
  │     weekLabel, dateRange, barChartData,
  │     totalIncome, totalExpense, profit, dailyDetails
  │   )
  │
  ▼
Render:
  ├── TabRow: [Mingguan●] [Bulanan] [Custom]
  ├── WeekNavigator(label, onPrev, onNext)
  ├── BarChart(data = 7 bars × 2 series)
  ├── SummaryRow(income, expense)
  ├── ProfitHeroCard(profit)
  └── DailyDetailList(dailyDetails)
```

---

## US-06: Lihat Laporan Bulanan

> **Sebagai** driver ojol,
> **saya ingin** melihat ringkasan bulanan dan breakdown per kategori,
> **agar** saya tahu kemana uang saya paling banyak keluar.

### Acceptance Criteria

| ID | Kriteria | Detail |
|----|----------|--------|
| AC-06.1 | Month Navigation | Chevron kiri/kanan untuk navigasi bulan. Label: "Oktober 2024" |
| AC-06.2 | Summary Cards | Pemasukan total + Pengeluaran total + Profit bulan ini |
| AC-06.3 | Category Breakdown | List per kategori: icon + nama + total nominal + persentase dari total. Sorted by amount DESC |
| AC-06.4 | Bar/Pie Option | Pilihan visualisasi: bar chart atau summary list (minimal: list view) |

### UF-06: Monthly Report Flow

```
ReportScreen → Tap tab "Bulanan"
  │
  ▼
ReportViewModel.loadMonthlyReport(monthOffset = 0)
  │
  ├── GetMonthlyReportUseCase.invoke(year, month)
  │     └── TransactionRepository.getGroupedByCategory(start, end)
  │           └── TransactionDao.getCategorySummary(start, end)
  │               → returns List<CategorySummary> {
  │                   category: String,
  │                   type: String,
  │                   totalAmount: Long,
  │                   count: Int
  │               }
  │
  ├── Separate into incomeCategories + expenseCategories
  ├── Calculate percentages per category
  │
  ▼
Render:
  ├── MonthNavigator("Oktober 2024", onPrev, onNext)
  ├── SummaryCards(income, expense, profit)
  ├── SectionHeader("Pengeluaran per Kategori")
  └── CategoryBreakdownList(expenseCategories)
```

---

## US-07: Lihat Laporan Custom Range

> **Sebagai** driver ojol,
> **saya ingin** memilih rentang tanggal tertentu untuk melihat laporan keuangan,
> **agar** saya bisa analisis periode yang saya mau (contoh: tanggal 1-15).

### Acceptance Criteria

| ID | Kriteria | Detail |
|----|----------|--------|
| AC-07.1 | Date Range Picker | Material 3 `DateRangePicker` untuk pilih tanggal mulai dan selesai |
| AC-07.2 | Validation | End date harus ≥ start date. Max range: 1 tahun |
| AC-07.3 | Report Content | Sama seperti mingguan: summary cards + profit + daily detail list |

---

## US-08: Export Laporan ke CSV

> **Sebagai** driver ojol,
> **saya ingin** mengekspor data transaksi ke file CSV,
> **agar** saya bisa menyimpan atau membaginya.

### Acceptance Criteria

| ID | Kriteria | Detail |
|----|----------|--------|
| AC-08.1 | Trigger | Menu item di toolbar ReportScreen (icon `more_vert` → "Export CSV") |
| AC-08.2 | Scope | Export semua transaksi dalam range yang sedang dilihat (weekly/monthly/custom) |
| AC-08.3 | CSV Format | Header: `id,tanggal,tipe,nominal,kategori,catatan`. Encoding: UTF-8. Separator: koma |
| AC-08.4 | Share | File disimpan di app cache → buka Android ShareSheet via `FileProvider` |
| AC-08.5 | Filename | Format: `driver_wallet_YYYY-MM-DD_to_YYYY-MM-DD.csv` |

### UF-08: Export Flow

```
ReportScreen → Tap ⋮ Menu → "Export CSV"
  │
  ▼
ReportViewModel.exportCsv(startDate, endDate)
  │
  ├── ExportCsvUseCase.invoke(start, end)
  │     ├── TransactionRepository.getRange(start, end)
  │     │     └── TransactionDao.getByDateRange(start, end)
  │     │
  │     ├── Format rows:
  │     │     transactions.map { t ->
  │     │       "${t.id},${t.createdAt},${t.type},${t.amount},${t.category},${t.note}"
  │     │     }
  │     │
  │     └── Write to file:
  │           context.cacheDir / "exports" / filename
  │           → returns: File
  │
  ├── Create ShareIntent:
  │     val uri = FileProvider.getUriForFile(context, authority, file)
  │     Intent(ACTION_SEND).apply {
  │       type = "text/csv"
  │       putExtra(EXTRA_STREAM, uri)
  │       addFlags(FLAG_GRANT_READ_URI_PERMISSION)
  │     }
  │
  └── Launch ShareSheet
```

---

## US-09: Atur Budget Harian

> **Sebagai** driver ojol,
> **saya ingin** mengatur budget harian per kategori (BBM, Makan, Rokok, Pulsa),
> **agar** Dashboard bisa menunjukkan sisa budget saya hari ini.

### Acceptance Criteria

| ID | Kriteria | Detail |
|----|----------|--------|
| AC-09.1 | Budget Fields | 4 FilledTextField (M3 style): BBM, Makan, Rokok, Pulsa/Data. Input numeric, prefix "Rp" |
| AC-09.2 | Default Values | Jika belum diset: default semua 0 |
| AC-09.3 | Save | Tap "Simpan Perubahan" → simpan ke Room `daily_budgets` table |
| AC-09.4 | Validation | Nominal harus ≥ 0, Integer. Tampilkan error jika input tidak valid |
| AC-09.5 | Dashboard Integration | Budget total = SUM(daily_budgets.amount). Dipakai di BudgetRemainingCard di Dashboard |

### UF-09: Budget Settings Flow

```
BottomNav "Pengaturan" tap
  │
  ▼
SettingsScreen
  │
  ▼
SettingsViewModel.loadSettings()
  │
  ├── DailyBudgetRepository.getAll()
  │     └── DailyBudgetDao.getAll()
  │         → returns: List<DailyBudget>  // bbm=30000, makan=50000, rokok=25000, pulsa=5000
  │
  ▼
Render Section "Budget Harian":
  ├── FilledTextField(label="BBM", value=30000)
  ├── FilledTextField(label="Makan", value=50000)
  ├── FilledTextField(label="Rokok", value=25000)
  └── FilledTextField(label="Pulsa / Data", value=5000)

User edits values → ViewModel tracks dirty state
  │
  Tap "Simpan Perubahan"
  │
  ▼
SettingsViewModel.saveAll()
  ├── SaveDailyBudgetsUseCase.invoke(budgets)
  │     └── DailyBudgetDao.upsertAll(budgets)
  ├── Snackbar: "Pengaturan tersimpan"
  └── Mark dirty = false
```

---

## US-10: Atur Pengeluaran Tetap

> **Sebagai** driver ojol,
> **saya ingin** mencatat pengeluaran tetap bulanan (kontrakan, SPP) dan harian (parkir),
> **agar** target harian saya dihitung dengan memperhitungkan kewajiban tetap.

### Acceptance Criteria

| ID | Kriteria | Detail |
|----|----------|--------|
| AC-10.1 | Monthly List | List item: icon (circle, `secondaryContainer`) + nama + deskripsi + nominal. Contoh: 🏠 Rumah Tangga / Bayar Kontrakan / Rp 800rb |
| AC-10.2 | Daily List | Sama format, untuk expense harian tetap. Contoh: 🅿️ Parkir / Langganan Stasiun / Rp 2rb |
| AC-10.3 | Tambah | Tombol "Tambah" per section → Dialog/BottomSheet form: icon picker (Material icon), nama, deskripsi, nominal |
| AC-10.4 | Edit | Tap item → form pre-filled → edit |
| AC-10.5 | Hapus | Swipe-to-dismiss atau long-press → dialog konfirmasi → soft delete |
| AC-10.6 | Dashboard Integration | Monthly expenses di-prorata harian: `monthlyAmount / daysInMonth`. Daily expenses langsung dijumlahkan. Keduanya mempengaruhi DailyTarget formula |

### Data Model

```kotlin
// Pengeluaran Tetap Bulanan
MonthlyExpense(
    id: String,
    name: String,          // "Rumah Tangga"
    description: String,   // "Bayar Kontrakan"
    icon: String,          // Material icon name: "home"
    amount: Long,          // 800_000
    isDeleted: Boolean,
)

// Pengeluaran Tetap Harian
DailyExpense(
    id: String,
    name: String,          // "Parkir"
    description: String,   // "Langganan Stasiun"
    icon: String,          // "local_parking"
    amount: Long,          // 2_000
    isDeleted: Boolean,
)
```

---

## US-11: Atur Target Tanggal Lunas

> **Sebagai** driver ojol yang punya hutang,
> **saya ingin** menentukan target tanggal untuk melunasi semua hutang,
> **agar** app bisa menghitung berapa yang harus saya hasilkan per hari.

### Acceptance Criteria

| ID | Kriteria | Detail |
|----|----------|--------|
| AC-11.1 | Date Display | Menampilkan tanggal target saat ini, contoh: "31 Des 2024" |
| AC-11.2 | Edit | Tap → Material 3 DatePicker dialog → pilih tanggal |
| AC-11.3 | Validation | Tanggal harus di masa depan (> today) |
| AC-11.4 | Save | Simpan ke Settings key-value: `debt_target_date = "2024-12-31"` |
| AC-11.5 | Dashboard Integration | Dipakai di DailyTarget formula: `workingDaysRemaining = workingDays antara today dan debt_target_date` |

### UF-11: Target Date Flow

```
SettingsScreen → Tap "Target tanggal lunas" row
  │
  ▼
Show Material 3 DatePickerDialog
  │
  ├── Initial date = current target (atau today + 30 days jika belum diset)
  ├── Min selectable = tomorrow
  │
  User picks date → Tap "OK"
  │
  ▼
SettingsViewModel.updateTargetDate(selectedDate)
  │
  ├── SettingsRepository.put("debt_target_date", date.toIsoString())
  │     └── SettingsDao.upsert(key, value)
  │
  └── Update UI display
```

---

## US-12: Onboarding Pertama Kali

> **Sebagai** pengguna baru,
> **saya ingin** mendapat pengenalan singkat tentang fitur app saat pertama buka,
> **agar** saya langsung paham cara menggunakannya.

### Acceptance Criteria

| ID | Kriteria | Detail |
|----|----------|--------|
| AC-12.1 | Trigger | Muncul hanya sekali, saat `DataStore.has_seen_onboarding == false` |
| AC-12.2 | Pages | 3-4 halaman: (1) Dashboard overview, (2) Quick Input, (3) Kelola Hutang, (4) Laporan. Setiap halaman: ilustrasi/icon + judul + deskripsi singkat |
| AC-12.3 | Navigation | Swipe horizontal (`HorizontalPager`) + dot indicator |
| AC-12.4 | Skip/Complete | Tombol "Lewati" (setiap halaman) + tombol "Mulai" (halaman terakhir) |
| AC-12.5 | Persist | Setelah dismiss: `DataStore.set(has_seen_onboarding = true)`. Tidak muncul lagi |

---

## US-13: Navigasi Antar Screen

> **Sebagai** pengguna,
> **saya ingin** berpindah antar fitur dengan cepat lewat bottom navigation bar,
> **agar** saya tidak perlu banyak langkah untuk mengakses fitur yang saya mau.

### Acceptance Criteria

| ID | Kriteria | Detail |
|----|----------|--------|
| AC-13.1 | 5 Menu Items | Beranda, Input, Hutang, Laporan, Pengaturan. Konsisten di semua screen |
| AC-13.2 | Active State | Item aktif: icon filled + label bold + indicator pill (`secondaryContainer`) |
| AC-13.3 | Badge | Item Hutang: red dot badge jika ada cicilan overdue/critical (≤ 3 hari) |
| AC-13.4 | Navigation 3 | Implementasi Navigation 3 dengan type-safe routes dan user-owned backstack |
| AC-13.5 | State Preservation | Saat switch tab, state screen sebelumnya harus preserved (tidak reset) |
| AC-13.6 | Scaffold | Semua screen dibungkus satu `Scaffold` dengan shared `NavigationBar` dan `SnackbarHost` |

### Navigation Architecture

```kotlin
// Type-safe routes (Navigation 3)
@Serializable data object DashboardRoute
@Serializable data object QuickInputRoute
@Serializable data object DebtListRoute
@Serializable data class DebtFormRoute(val debtId: String? = null)
@Serializable data object ReportRoute
@Serializable data object SettingsRoute

// User-owned backstack
val backStack = rememberMutableStateListOf<Any>(DashboardRoute)

// NavDisplay mapping
NavDisplay(
    backStack = backStack,
    entryProvider = entryProvider {
        entry<DashboardRoute> { DashboardScreen(...) }
        entry<QuickInputRoute> { QuickInputScreen(...) }
        entry<DebtListRoute> { DebtListScreen(...) }
        entry<DebtFormRoute> { DebtFormScreen(debtId = it.debtId, ...) }
        entry<ReportRoute> { ReportScreen(...) }
        entry<SettingsRoute> { SettingsScreen(...) }
    }
)
```

---

## Global UiEvent Contract

Semua ViewModel menggunakan pola yang sama untuk one-shot events:

```kotlin
// Base UiEvent yang bisa dipakai semua screen
sealed interface GlobalUiEvent {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val duration: SnackbarDuration = SnackbarDuration.Short,
    ) : GlobalUiEvent

    data class Navigate(val route: Any) : GlobalUiEvent
    data object NavigateBack : GlobalUiEvent
}

// Collect di Composable:
LaunchedEffect(Unit) {
    viewModel.uiEvent.collect { event ->
        when (event) {
            is GlobalUiEvent.ShowSnackbar -> {
                snackbarHostState.showSnackbar(
                    message = event.message,
                    actionLabel = event.actionLabel,
                    duration = event.duration,
                )
            }
            is GlobalUiEvent.Navigate -> backStack.add(event.route)
            is GlobalUiEvent.NavigateBack -> backStack.removeLastOrNull()
        }
    }
}
```

---

*Dokumen ini adalah bagian 2 dari 5. Lanjut ke [AD_03_Tech_Architecture.md](./AD_03_Tech_Architecture.md) untuk arsitektur teknis dan project structure.*
