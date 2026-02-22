# AD_05: Implementation Guide — Driver Wallet Android

> **Audience:** AI Builder & Developer
> **Prerequisite:** Baca semua dokumen sebelumnya (AD_01 – AD_04)
> **Tujuan:** Panduan langkah-demi-langkah untuk membangun app dari nol

---

## 1. Build Order (Urutan Implementasi)

App dibangun secara **bottom-up**: data layer dulu, baru domain, terakhir UI.

### Phase 0: Project Setup

```
Priority: █████ CRITICAL (harus benar sebelum menulis kode fitur)
Estimasi: 1-2 jam
```

| Step | Task | File(s) | Acceptance |
|------|------|---------|------------|
| 0.1 | Create Android project | `build.gradle.kts`, `settings.gradle.kts` | Project compiles |
| 0.2 | Setup Version Catalog | `gradle/libs.versions.toml` | Semua dependency terdaftar |
| 0.3 | Configure `app/build.gradle.kts` | Lihat AD_03 Section 11 | All plugins: KSP, Hilt, Room, Serialization |
| 0.4 | Create `DriverWalletApp.kt` | `@HiltAndroidApp` | App launches |
| 0.5 | Create `MainActivity.kt` | `@AndroidEntryPoint`, `enableEdgeToEdge()` | Blank screen, no crash |
| 0.6 | Setup Theme | `Theme.kt`, `Color.kt`, `Type.kt`, `Shape.kt` | Dynamic Color works |
| 0.7 | Setup Room Database | `AppDatabase.kt`, `Converters.kt`, `DatabaseModule.kt` | DB created on launch |
| 0.8 | Setup DataStore | `AppDataStore.kt`, `DataStoreModule.kt` | Read/write works |
| 0.9 | Setup Navigation shell | `Routes.kt`, `AppNavigation.kt`, `BottomNavBar.kt` | 5 tabs navigable, placeholder screens |

### Phase 1: Core Data Layer

```
Priority: █████ CRITICAL
Estimasi: 2-3 jam
```

| Step | Task | File(s) | Acceptance |
|------|------|---------|------------|
| 1.1 | Define all Room Entities | 7 entity files (lihat AD_04 Section 2) | Compiles, schema exported |
| 1.2 | Define all DAOs | 7 DAO interfaces (lihat AD_04 Section 3) | Compiles |
| 1.3 | Register entities & DAOs in AppDatabase | `AppDatabase.kt` | `version = 1` builds |
| 1.4 | Create `DatabaseCallback` (seed data) | `DatabaseCallback.kt` | First launch seeds budgets + settings |
| 1.5 | Define Categories constant | `Categories.kt`, `TransactionType.kt` | All categories accessible |
| 1.6 | Define Domain models | Lihat AD_04 Section 6 | All `@Immutable` models |
| 1.7 | Create Entity ↔ Domain mappers | `TransactionMapper.kt` | `toDomain()` / `toEntity()` tested |
| 1.8 | Create Utility classes | `CurrencyFormatter.kt`, `DateTimeExt.kt` | Unit tests pass |

### Phase 2: Shared Repositories

```
Priority: █████ CRITICAL
Estimasi: 1-2 jam
```

| Step | Task | File(s) | Acceptance |
|------|------|---------|------------|
| 2.1 | `TransactionRepository` interface | Domain layer | Defined |
| 2.2 | `TransactionRepositoryImpl` | Data layer, `@Inject` | Reads + writes to Room |
| 2.3 | `DebtRepository` interface | Domain layer | Defined |
| 2.4 | `DebtRepositoryImpl` | Data layer, `@Inject`, includes `payInstallment` `@Transaction` | Atomic payment works |
| 2.5 | `SettingsRepository` interface | Domain layer | Defined |
| 2.6 | `SettingsRepositoryImpl` | Data layer, `@Inject` | CRUD budget, expense, settings |
| 2.7 | `RepositoryModule` (Hilt bindings) | `@Binds` interface → impl | Hilt resolves correctly |

### Phase 3: Feature — Quick Input (F02)

```
Priority: █████ HIGH (Fitur paling sering dipakai)
Estimasi: 3-4 jam
```

**Build Quick Input PERTAMA karena ini adalah fitur utama untuk memasukkan data.**
Tanpa input, tidak ada data untuk Dashboard atau Report.

| Step | Task | File(s) | Acceptance |
|------|------|---------|------------|
| 3.1 | `SaveTransactionUseCase` | Domain layer | Validates + saves |
| 3.2 | `QuickInputUiState` | Sealed interface | Loading, Ready |
| 3.3 | `QuickInputUiAction` | Sealed interface | All user actions |
| 3.4 | `QuickInputUiEvent` | Uses `GlobalUiEvent` | ShowSnackbar, NavigateBack |
| 3.5 | `QuickInputViewModel` | `@HiltViewModel` | Processes actions, emits state |
| 3.6 | `TypeToggle` | Composable | Income/Expense toggle |
| 3.7 | `CategoryGrid` | Composable | Dynamic grid based on type |
| 3.8 | `AmountDisplay` | Composable | 80sp number, formatted |
| 3.9 | `PresetButtons` | Composable | +5rb, +10rb, +20rb, +50rb, +100rb |
| 3.10 | `NumberPad` | Composable | 1-9, 000, 0, backspace |
| 3.11 | `NoteInput` | Composable | Optional text field |
| 3.12 | `SaveButton` | Composable | Disabled jika amount = 0 |
| 3.13 | `QuickInputScreen` | Screen composable | Full flow works: tap → save → snackbar |

### Phase 4: Feature — Dashboard (F01)

```
Priority: █████ HIGH
Estimasi: 3-4 jam
```

| Step | Task | File(s) | Acceptance |
|------|------|---------|------------|
| 4.1 | `GetDashboardSummaryUseCase` | Orchestrates 5 data sources | Returns `DashboardData` |
| 4.2 | `CalculateDailyTargetUseCase` | Implements formula dari AD_01 | Correct calculation |
| 4.3 | `DashboardUiState` | Sealed interface | Loading, Success, Error |
| 4.4 | `DashboardViewModel` | `@HiltViewModel` | Loads on init, refresh |
| 4.5 | `ProfitHeroCard` | Composable | Shows profit/loss with color |
| 4.6 | `IncomeExpenseRow` | Composable | Two summary cards |
| 4.7 | `DailyTargetSection` | Composable | Progress bar + target amount |
| 4.8 | `BudgetRemainingCard` | Composable | Budget vs spent |
| 4.9 | `DueAlertCard` | Composable | Urgency-colored alert |
| 4.10 | `TodayTransactionList` | Composable | Recent 5 + "Lihat Semua" |
| 4.11 | `DashboardScreen` | Screen composable | All sections render with data |

### Phase 5: Feature — Debt Management (F05)

```
Priority: ████ MEDIUM-HIGH
Estimasi: 4-5 jam
```

| Step | Task | File(s) | Acceptance |
|------|------|---------|------------|
| 5.1 | `GetActiveDebtsUseCase` | Returns `Flow<List<DebtWithScheduleInfo>>` | Emits on change |
| 5.2 | `SaveDebtUseCase` | Validates + saves + generates schedules | Schedules auto-generated |
| 5.3 | `PayDebtInstallmentUseCase` | Calls `repository.payInstallment()` | Atomic 4-step payment |
| 5.4 | `DebtListUiState` | Sealed interface | Loading, Success, Empty |
| 5.5 | `DebtListViewModel` | `@HiltViewModel` | Loads debts + handles payment |
| 5.6 | `DebtHeroCard` | Composable | Total debt remaining |
| 5.7 | `DebtCardItem` | Composable | Card with progress bar, urgency color |
| 5.8 | `PaymentBottomSheet` | Composable | ModalBottomSheet, pre-filled amount, confirm |
| 5.9 | `DebtListScreen` | Screen composable | List + FAB + BottomSheet |
| 5.10 | `DebtFormUiState` | Sealed interface | Form state |
| 5.11 | `DebtFormViewModel` | `@HiltViewModel` | Validate + save |
| 5.12 | `DebtFormScreen` | Screen composable | Full form with all fields |

### Phase 6: Feature — Reports (F06)

```
Priority: ███ MEDIUM
Estimasi: 3-4 jam
```

| Step | Task | File(s) | Acceptance |
|------|------|---------|------------|
| 6.1 | `GetWeeklyReportUseCase` | Returns `WeeklyReport` | 7-day summary with navigation |
| 6.2 | `GetMonthlyReportUseCase` | Returns `MonthlyReport` | Category breakdown |
| 6.3 | `GetCustomReportUseCase` | Date picker range | Custom range summary |
| 6.4 | `ExportCsvUseCase` | Generates CSV file | FileProvider + ShareSheet |
| 6.5 | `ReportUiState` | Sealed interface + tab enum | Weekly, Monthly, Custom tabs |
| 6.6 | `ReportViewModel` | `@HiltViewModel` | Tab switching, week navigation |
| 6.7 | `ReportTabRow` | Composable | 3 tabs with M3 style |
| 6.8 | `WeekNavigator` | Composable | ◀ Minggu Ini ▶ with swipe |
| 6.9 | `BarChartView` | Composable Canvas | 7-bar chart, income/expense colors |
| 6.10 | `SummaryCards` | Composable | Income, Expense, Profit cards |
| 6.11 | `DailyDetailList` | Composable | Expandable daily breakdown |
| 6.12 | `CategoryBreakdownList` | Composable | Sorted category list with % |
| 6.13 | `ReportScreen` | Screen composable | All 3 tabs work |

### Phase 7: Feature — Settings (F07)

```
Priority: ███ MEDIUM
Estimasi: 2-3 jam
```

| Step | Task | File(s) | Acceptance |
|------|------|---------|------------|
| 7.1 | `SaveDailyBudgetsUseCase` | Validates + upserts | All 4 budgets saved |
| 7.2 | `SaveMonthlyExpenseUseCase` | CRUD monthly expense | Add, edit, soft delete |
| 7.3 | `SaveDailyExpenseUseCase` | CRUD daily expense | Add, edit, soft delete |
| 7.4 | `SettingsUiState` | Data class | All settings fields |
| 7.5 | `SettingsViewModel` | `@HiltViewModel` | Loads + saves all settings |
| 7.6 | `DarkModeToggle` | Composable | Switch with DataStore |
| 7.7 | `BudgetSection` | Composable | 4 FilledTextField for budgets |
| 7.8 | `TargetDateRow` | Composable | DatePicker for debt target |
| 7.9 | `FixedExpenseSection` | Composable | List + Add/Edit/Delete |
| 7.10 | `ExpenseFormDialog` | Composable | AlertDialog for add/edit |
| 7.11 | `SettingsScreen` | Screen composable | All settings save correctly |

### Phase 8: Feature — Onboarding (F08)

```
Priority: ██ LOW
Estimasi: 1 jam
```

| Step | Task | File(s) | Acceptance |
|------|------|---------|------------|
| 8.1 | `OnboardingPage` data class | Pages content | 3 pages defined |
| 8.2 | `OnboardingOverlay` | HorizontalPager + dots | Swipe through pages |
| 8.3 | Integrate in `AppNavigation` | Check DataStore flag | Shows once, never again |

### Phase 9: Polish & Testing

```
Priority: ████ HIGH
Estimasi: 2-3 jam
```

| Step | Task | Acceptance |
|------|------|-----------|
| 9.1 | Unit tests (ViewModel + UseCase) | > 80% coverage on business logic |
| 9.2 | DAO instrumented tests | All queries return correct data |
| 9.3 | Compose UI tests | Critical flows: input → save, debt payment |
| 9.4 | Dark mode verification | All screens look correct |
| 9.5 | Accessibility pass | TalkBack reads all screens correctly |
| 9.6 | ProGuard test | Release build works without crash |
| 9.7 | Edge cases | Empty states, max values, timezone boundary |

---

## 2. Screen Implementation Guide

### 2.1 Quick Input Screen (Most Complex)

#### Architecture

```
QuickInputScreen
  ├─ observes → QuickInputViewModel.uiState (StateFlow)
  ├─ sends    → QuickInputViewModel.onAction(UiAction)
  └─ collects → QuickInputViewModel.uiEvent (Channel)

QuickInputViewModel
  ├─ uses → SaveTransactionUseCase
  └─ manages → amount (Long), type, category, note
```

#### Complete UiState / UiAction / UiEvent

```kotlin
// file: feature/input/ui/QuickInputUiState.kt
sealed interface QuickInputUiState {
    data object Loading : QuickInputUiState

    @Immutable
    data class Ready(
        val type: TransactionType = TransactionType.INCOME,
        val amount: Long = 0L,
        val displayAmount: String = "0",      // Formatted: "150.000"
        val selectedCategory: Category? = null,
        val categories: List<Category> = emptyList(),
        val note: String = "",
        val isSaving: Boolean = false,
    ) : QuickInputUiState {
        val canSave: Boolean get() = amount > 0 && selectedCategory != null && !isSaving
    }
}

// file: feature/input/ui/QuickInputUiAction.kt
sealed interface QuickInputUiAction {
    data class SwitchType(val type: TransactionType) : QuickInputUiAction
    data class SelectCategory(val category: Category) : QuickInputUiAction
    data class AppendDigit(val digit: String) : QuickInputUiAction  // "0"-"9", "000"
    data object Backspace : QuickInputUiAction
    data class AddPreset(val amount: Long) : QuickInputUiAction     // 5000, 10000, etc.
    data class UpdateNote(val note: String) : QuickInputUiAction
    data object Save : QuickInputUiAction
}

// file: core/ui/navigation/GlobalUiEvent.kt (shared across all features)
sealed interface GlobalUiEvent {
    data class ShowSnackbar(val message: String) : GlobalUiEvent
    data class Navigate(val route: Any) : GlobalUiEvent
    data object NavigateBack : GlobalUiEvent
}
```

#### ViewModel Implementation

```kotlin
// file: feature/input/ui/QuickInputViewModel.kt
@HiltViewModel
class QuickInputViewModel @Inject constructor(
    private val saveTransaction: SaveTransactionUseCase,
) : ViewModel() {

    companion object {
        private const val MAX_AMOUNT = 999_999_999L
        private const val MAX_DIGITS = 9  // 999.999.999
    }

    private val _uiState = MutableStateFlow<QuickInputUiState>(
        QuickInputUiState.Ready(
            categories = Categories.getByType(TransactionType.INCOME),
        )
    )
    val uiState: StateFlow<QuickInputUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<GlobalUiEvent>(Channel.BUFFERED)
    val uiEvent: Flow<GlobalUiEvent> = _uiEvent.receiveAsFlow()

    fun onAction(action: QuickInputUiAction) {
        val current = _uiState.value as? QuickInputUiState.Ready ?: return

        when (action) {
            is QuickInputUiAction.SwitchType -> {
                _uiState.value = current.copy(
                    type = action.type,
                    categories = Categories.getByType(action.type),
                    selectedCategory = null,  // Reset on type switch
                )
            }

            is QuickInputUiAction.SelectCategory -> {
                _uiState.value = current.copy(selectedCategory = action.category)
            }

            is QuickInputUiAction.AppendDigit -> {
                val currentDigits = if (current.amount == 0L) "" else current.amount.toString()
                val newDigits = currentDigits + action.digit
                val newAmount = newDigits.toLongOrNull() ?: current.amount

                if (newAmount <= MAX_AMOUNT && newDigits.length <= MAX_DIGITS) {
                    _uiState.value = current.copy(
                        amount = newAmount,
                        displayAmount = CurrencyFormatter.format(newAmount),
                    )
                }
            }

            is QuickInputUiAction.Backspace -> {
                val currentStr = current.amount.toString()
                val newStr = currentStr.dropLast(1)
                val newAmount = newStr.toLongOrNull() ?: 0L
                _uiState.value = current.copy(
                    amount = newAmount,
                    displayAmount = if (newAmount == 0L) "0" else CurrencyFormatter.format(newAmount),
                )
            }

            is QuickInputUiAction.AddPreset -> {
                // ADDITIVE: preset ditambahkan ke amount yang ada
                val newAmount = (current.amount + action.amount).coerceAtMost(MAX_AMOUNT)
                _uiState.value = current.copy(
                    amount = newAmount,
                    displayAmount = CurrencyFormatter.format(newAmount),
                )
            }

            is QuickInputUiAction.UpdateNote -> {
                val trimmed = action.note.take(100)  // Max 100 chars
                _uiState.value = current.copy(note = trimmed)
            }

            is QuickInputUiAction.Save -> save(current)
        }
    }

    private fun save(state: QuickInputUiState.Ready) {
        if (!state.canSave) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)

            try {
                saveTransaction(
                    type = state.type,
                    amount = state.amount,
                    category = state.selectedCategory!!,
                    note = state.note,
                )

                val typeLabel = when (state.type) {
                    TransactionType.INCOME -> "Pemasukan"
                    TransactionType.EXPENSE -> "Pengeluaran"
                }
                _uiEvent.send(
                    GlobalUiEvent.ShowSnackbar(
                        "$typeLabel ${CurrencyFormatter.formatWithPrefix(state.amount)} tersimpan"
                    )
                )

                // Reset form
                _uiState.value = QuickInputUiState.Ready(
                    type = state.type,
                    categories = state.categories,
                )

            } catch (e: Exception) {
                _uiState.value = state.copy(isSaving = false)
                _uiEvent.send(GlobalUiEvent.ShowSnackbar("Gagal menyimpan: ${e.message}"))
            }
        }
    }
}
```

#### UseCase Implementation

```kotlin
// file: feature/input/domain/SaveTransactionUseCase.kt
class SaveTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(
        type: TransactionType,
        amount: Long,
        category: Category,
        note: String,
    ) {
        // Validation
        require(amount > 0) { "Amount must be positive" }
        require(amount <= 999_999_999) { "Amount too large" }
        require(note.length <= 100) { "Note too long" }

        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            createdAt = nowJakarta(),
            type = type,
            amount = amount,
            category = category,
            note = note.trim(),
            source = "manual",
            debtId = null,
        )

        repository.insert(transaction)
    }
}
```

---

### 2.2 Dashboard Screen

#### DailyTarget Formula Implementation

```kotlin
// file: feature/dashboard/domain/CalculateDailyTargetUseCase.kt
class CalculateDailyTargetUseCase @Inject constructor(
    private val debtDao: DebtDao,
    private val monthlyExpenseDao: MonthlyExpenseDao,
    private val dailyExpenseDao: DailyExpenseDao,
    private val dailyBudgetDao: DailyBudgetDao,
    private val settingsDao: SettingsDao,
) {
    /**
     * Formula dari AD_01 PRD:
     *
     * totalDebtRemaining = SUM(debts.remaining_amount) WHERE active
     * daysUntilTarget = target_date - today
     * workDays = daysUntilTarget - (restDaysPerWeek / 7 * daysUntilTarget)
     * dailyDebtTarget = totalDebtRemaining / workDays
     *
     * monthlyFixed = SUM(monthly_expenses.amount)
     * dailyProrated = monthlyFixed / 30
     *
     * dailyFixed = SUM(daily_expenses.amount)
     * dailyBudget = SUM(daily_budgets.amount)
     *
     * TARGET = dailyDebtTarget + dailyProrated + dailyFixed + dailyBudget
     */
    suspend operator fun invoke(today: LocalDate): DailyTarget {
        // 1. Get inputs
        val totalDebtRemaining = debtDao.getTotalRemaining()
        val targetDateStr = settingsDao.getValue("debt_target_date")
        val restDaysStr = settingsDao.getValue("rest_days") ?: "0"

        // 2. Parse settings
        val targetDate = targetDateStr?.let { LocalDate.parse(it) }
            ?: today.plusMonths(3)  // fallback: 3 bulan
        val restDaysPerWeek = restDaysStr.split(",")
            .filter { it.isNotBlank() }
            .size

        // 3. Calculate work days
        val daysUntilTarget = ChronoUnit.DAYS.between(today, targetDate).toInt()
            .coerceAtLeast(1)  // Minimal 1 hari
        val workDaysRatio = 1.0 - (restDaysPerWeek.toDouble() / 7.0)
        val workDays = (daysUntilTarget * workDaysRatio).toInt().coerceAtLeast(1)

        // 4. Calculate components
        val dailyDebtTarget = if (totalDebtRemaining > 0) {
            totalDebtRemaining / workDays
        } else 0L

        val monthlyFixed = monthlyExpenseDao.getTotalAmount()
        val dailyProrated = monthlyFixed / 30  // integer division

        val dailyFixed = dailyExpenseDao.getTotalAmount()
        val dailyBudget = dailyBudgetDao.getTotalBudget()

        // 5. Total target
        val targetAmount = dailyDebtTarget + dailyProrated + dailyFixed + dailyBudget

        // 6. Compare with today's income
        val earnedToday = 0L  // Will be passed from GetDashboardSummaryUseCase

        return DailyTarget(
            earnedToday = earnedToday,  // Filled by caller
            targetAmount = targetAmount,
            isOnTrack = earnedToday >= targetAmount,
            percentage = if (targetAmount > 0) {
                ((earnedToday.toDouble() / targetAmount) * 100).toInt().coerceIn(0, 999)
            } else 100,
        )
    }
}
```

#### GetDashboardSummaryUseCase

```kotlin
// file: feature/dashboard/domain/GetDashboardSummaryUseCase.kt
class GetDashboardSummaryUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val calculateDailyTarget: CalculateDailyTargetUseCase,
    private val dailyBudgetDao: DailyBudgetDao,
    private val debtScheduleDao: DebtScheduleDao,
) {
    suspend operator fun invoke(today: LocalDate): DashboardData {
        val dateStr = today.toString()  // ISO format

        // 1. Today summary
        val summaryRaw = transactionDao.getTodaySummary(dateStr)
        val todaySummary = TodaySummary(
            totalIncome = summaryRaw.totalIncome,
            totalExpense = summaryRaw.totalExpense,
            totalDebtPayment = summaryRaw.totalDebtPayment,
            transactionCount = summaryRaw.transactionCount,
        )

        // 2. Daily target (inject today's income)
        val dailyTarget = calculateDailyTarget(today).copy(
            earnedToday = summaryRaw.totalIncome,
            isOnTrack = summaryRaw.totalIncome >= calculateDailyTarget(today).targetAmount,
            percentage = if (calculateDailyTarget(today).targetAmount > 0) {
                ((summaryRaw.totalIncome.toDouble() / calculateDailyTarget(today).targetAmount) * 100)
                    .toInt().coerceIn(0, 999)
            } else 100,
        )

        // 3. Budget remaining
        val totalBudget = dailyBudgetDao.getTotalBudget()
        val spentList = transactionDao.getBudgetSpentToday(dateStr)
        val totalSpent = spentList.sumOf { it.totalSpent }
        val budgetInfo = BudgetInfo(
            totalBudget = totalBudget,
            totalSpent = totalSpent,
        )

        // 4. Due alerts (next 7 days)
        val maxDate = today.plusDays(7).toString()
        // Update overdue first
        debtScheduleDao.markOverdueSchedules(dateStr)
        val upcomingDue = debtScheduleDao.getUpcomingDue(maxDate).map { raw ->
            val dueDate = LocalDate.parse(raw.dueDate)
            val daysUntilDue = ChronoUnit.DAYS.between(today, dueDate).toInt()
            DueAlert(
                debtId = raw.debtId,
                platform = raw.platform,
                scheduleId = raw.id,
                dueDate = dueDate,
                amount = raw.amount,
                daysUntilDue = daysUntilDue,
                urgency = UrgencyLevel.fromDaysUntilDue(daysUntilDue),
            )
        }

        // 5. Recent transactions
        val recentTransactions = transactionDao.getRecentByDate(dateStr, limit = 5)

        return DashboardData(
            todaySummary = todaySummary,
            dailyTarget = dailyTarget,
            budgetInfo = budgetInfo,
            dueAlerts = upcomingDue,
            recentTransactions = recentTransactions.map { it.toDomain() },
        )
    }
}

@Immutable
data class DashboardData(
    val todaySummary: TodaySummary,
    val dailyTarget: DailyTarget,
    val budgetInfo: BudgetInfo,
    val dueAlerts: List<DueAlert>,
    val recentTransactions: List<Transaction>,
)
```

---

### 2.3 Debt Payment Flow (Atomic Transaction)

Ini adalah flow paling critical karena melibatkan multiple table updates:

```kotlin
// file: feature/debt/domain/PayDebtInstallmentUseCase.kt
class PayDebtInstallmentUseCase @Inject constructor(
    private val debtRepository: DebtRepository,
) {
    /**
     * Bayar cicilan hutang. Operasi ini ATOMIC:
     * 1. Update debt.remaining_amount
     * 2. Mark schedule as paid
     * 3. Insert expense transaction
     * 4. Check if fully paid → update debt status
     *
     * Jika salah satu gagal, semua di-rollback.
     */
    suspend operator fun invoke(
        debtId: String,
        scheduleId: String,
        amount: Long,
    ) {
        require(amount > 0) { "Payment amount must be positive" }
        debtRepository.payInstallment(debtId, scheduleId, amount)
    }
}
```

---

### 2.4 CSV Export Flow

```kotlin
// file: feature/report/domain/ExportCsvUseCase.kt
class ExportCsvUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    @ApplicationContext private val context: Context,
) {
    /**
     * Export transaksi ke CSV file.
     * Returns: Uri via FileProvider untuk di-share.
     */
    suspend operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Uri = withContext(Dispatchers.IO) {
        val transactions = transactionDao.getByDateRange(
            startDate.toString(),
            endDate.toString(),
        )

        // Build CSV content
        val header = "Tanggal,Tipe,Kategori,Jumlah,Catatan\n"
        val rows = transactions.joinToString("\n") { tx ->
            val date = tx.createdAt.substringBefore('T')
            val type = if (tx.type == "income") "Pemasukan" else "Pengeluaran"
            val category = Categories.findById(tx.category)?.label ?: tx.category
            val amount = tx.amount.toString()
            val note = tx.note.replace(",", ";")
                .replace("\n", " ")  // Sanitize
            "$date,$type,$category,$amount,$note"
        }
        val csv = header + rows

        // Write to cache dir
        val fileName = "driver_wallet_${startDate}_${endDate}.csv"
        val file = File(context.cacheDir, fileName)
        file.writeText(csv, Charsets.UTF_8)

        // Return FileProvider URI
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }
}
```

#### FileProvider Config

```xml
<!-- res/xml/file_paths.xml -->
<paths>
    <cache-path name="exports" path="." />
</paths>
```

```xml
<!-- AndroidManifest.xml -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

#### Share Intent

```kotlin
// Di ReportViewModel, setelah export berhasil:
fun shareCsv(uri: Uri, context: Context) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Kirim laporan via"))
}
```

---

### 2.5 Bar Chart (Canvas Composable)

```kotlin
// file: feature/report/ui/component/BarChartView.kt
@Composable
fun BarChartView(
    dailySummaries: List<DailySummary>,    // Always 7 items (Mon-Sun)
    modifier: Modifier = Modifier,
) {
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall

    // Find max value for scale
    val maxValue = dailySummaries.maxOf { maxOf(it.totalIncome, it.totalExpense) }
        .coerceAtLeast(1)  // Avoid division by zero

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .semantics {
                contentDescription = "Grafik batang pendapatan dan pengeluaran mingguan"
            }
    ) {
        val barWidth = size.width / (dailySummaries.size * 3)  // 2 bars + 1 gap per day
        val chartHeight = size.height * 0.85f  // Leave room for labels

        dailySummaries.forEachIndexed { index, summary ->
            val x = index * barWidth * 3

            // Income bar
            val incomeHeight = (summary.totalIncome.toFloat() / maxValue) * chartHeight
            drawRoundRect(
                color = incomeColor,
                topLeft = Offset(x, chartHeight - incomeHeight),
                size = Size(barWidth, incomeHeight),
                cornerRadius = CornerRadius(4.dp.toPx()),
            )

            // Expense bar
            val expenseHeight = (summary.totalExpense.toFloat() / maxValue) * chartHeight
            drawRoundRect(
                color = expenseColor,
                topLeft = Offset(x + barWidth + 4.dp.toPx(), chartHeight - expenseHeight),
                size = Size(barWidth, expenseHeight),
                cornerRadius = CornerRadius(4.dp.toPx()),
            )
        }
    }

    // Day labels below chart
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        dailySummaries.forEach { summary ->
            Text(
                text = summary.dayOfWeek.take(3),  // "Sen", "Sel", etc.
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
```

---

## 3. Shared UI Components

### 3.1 AmountText (Formatted Rupiah)

```kotlin
// file: core/ui/component/AmountText.kt
@Composable
fun AmountText(
    amount: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    prefix: String = "Rp ",
    useShortFormat: Boolean = false,
) {
    Text(
        text = if (useShortFormat) {
            "$prefix${CurrencyFormatter.formatShort(amount)}"
        } else {
            "$prefix${CurrencyFormatter.format(amount)}"
        },
        modifier = modifier,
        style = style,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
```

### 3.2 HeroCard

```kotlin
// file: core/ui/component/HeroCard.kt
@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.HeroCard,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = { Column(modifier = Modifier.padding(24.dp), content = content) },
    )
}
```

### 3.3 EmptyState

```kotlin
// file: core/ui/component/EmptyState.kt
@Composable
fun EmptyState(
    message: String,
    icon: ImageVector = Icons.Outlined.Inbox,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}
```

---

## 4. Testing Strategy

### 4.1 Test Pyramid

```
        ┌───────────┐
        │  E2E/UI   │  ← Compose Testing (critical paths only)
        │  Tests    │
        ├───────────┤
        │Integration│  ← Room DAO tests (androidTest)
        │  Tests    │
        ├───────────┤
        │   Unit    │  ← ViewModel + UseCase (JUnit5 + MockK + Turbine)
        │   Tests   │
        └───────────┘
```

### 4.2 Unit Test: ViewModel (JUnit5 + MockK + Turbine)

```kotlin
// file: test/.../feature/input/QuickInputViewModelTest.kt
@ExtendWith(MockKExtension::class)
class QuickInputViewModelTest {

    @MockK
    private lateinit var saveTransaction: SaveTransactionUseCase

    private lateinit var viewModel: QuickInputViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = QuickInputViewModel(saveTransaction)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Ready with income type`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem() as QuickInputUiState.Ready
            assertThat(state.type).isEqualTo(TransactionType.INCOME)
            assertThat(state.amount).isEqualTo(0L)
            assertThat(state.canSave).isFalse()
        }
    }

    @Test
    fun `AppendDigit should update amount`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onAction(QuickInputUiAction.AppendDigit("1"))
            viewModel.onAction(QuickInputUiAction.AppendDigit("5"))
            viewModel.onAction(QuickInputUiAction.AppendDigit("000"))

            // Skip intermediate emissions, get latest
            val state = expectMostRecentItem() as QuickInputUiState.Ready
            assertThat(state.amount).isEqualTo(15000L)
            assertThat(state.displayAmount).isEqualTo("15.000")
        }
    }

    @Test
    fun `AddPreset should be additive`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onAction(QuickInputUiAction.AppendDigit("1"))
            viewModel.onAction(QuickInputUiAction.AppendDigit("0"))
            viewModel.onAction(QuickInputUiAction.AppendDigit("000"))  // 10000
            viewModel.onAction(QuickInputUiAction.AddPreset(5_000))    // + 5000

            val state = expectMostRecentItem() as QuickInputUiState.Ready
            assertThat(state.amount).isEqualTo(15_000L)
        }
    }

    @Test
    fun `amount should not exceed MAX_AMOUNT`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            // Try to enter 10 digits (max is 9 = 999.999.999)
            repeat(10) {
                viewModel.onAction(QuickInputUiAction.AppendDigit("9"))
            }

            val state = expectMostRecentItem() as QuickInputUiState.Ready
            assertThat(state.amount).isEqualTo(999_999_999L)
        }
    }

    @Test
    fun `Save should emit snackbar and reset form`() = runTest {
        coEvery { saveTransaction(any(), any(), any(), any()) } returns Unit

        val incomeCategory = Categories.incomeCategories.first()

        viewModel.onAction(QuickInputUiAction.SelectCategory(incomeCategory))
        viewModel.onAction(QuickInputUiAction.AppendDigit("5"))
        viewModel.onAction(QuickInputUiAction.AppendDigit("0"))
        viewModel.onAction(QuickInputUiAction.AppendDigit("000"))

        viewModel.uiEvent.test {
            viewModel.onAction(QuickInputUiAction.Save)

            val event = awaitItem()
            assertThat(event).isInstanceOf(GlobalUiEvent.ShowSnackbar::class.java)
            assertThat((event as GlobalUiEvent.ShowSnackbar).message)
                .contains("tersimpan")
        }

        // Verify form is reset
        viewModel.uiState.test {
            val state = awaitItem() as QuickInputUiState.Ready
            assertThat(state.amount).isEqualTo(0L)
            assertThat(state.selectedCategory).isNull()
        }
    }

    @Test
    fun `SwitchType should reset category selection`() = runTest {
        val incomeCategory = Categories.incomeCategories.first()
        viewModel.onAction(QuickInputUiAction.SelectCategory(incomeCategory))

        viewModel.uiState.test {
            viewModel.onAction(QuickInputUiAction.SwitchType(TransactionType.EXPENSE))

            val state = expectMostRecentItem() as QuickInputUiState.Ready
            assertThat(state.type).isEqualTo(TransactionType.EXPENSE)
            assertThat(state.selectedCategory).isNull()
            assertThat(state.categories).isEqualTo(Categories.expenseCategories)
        }
    }
}
```

### 4.3 Unit Test: UseCase

```kotlin
// file: test/.../feature/debt/PayDebtInstallmentUseCaseTest.kt
@ExtendWith(MockKExtension::class)
class PayDebtInstallmentUseCaseTest {

    @MockK
    private lateinit var debtRepository: DebtRepository

    private lateinit var useCase: PayDebtInstallmentUseCase

    @BeforeEach
    fun setup() {
        useCase = PayDebtInstallmentUseCase(debtRepository)
    }

    @Test
    fun `should call repository payInstallment`() = runTest {
        coEvery { debtRepository.payInstallment(any(), any(), any()) } returns Unit

        useCase(debtId = "debt-1", scheduleId = "sched-1", amount = 500_000)

        coVerify(exactly = 1) {
            debtRepository.payInstallment("debt-1", "sched-1", 500_000)
        }
    }

    @Test
    fun `should reject zero amount`() = runTest {
        assertThrows<IllegalArgumentException> {
            useCase(debtId = "debt-1", scheduleId = "sched-1", amount = 0)
        }
    }

    @Test
    fun `should reject negative amount`() = runTest {
        assertThrows<IllegalArgumentException> {
            useCase(debtId = "debt-1", scheduleId = "sched-1", amount = -100)
        }
    }
}
```

### 4.4 Instrumented Test: Room DAO

```kotlin
// file: androidTest/.../core/database/TransactionDaoTest.kt
@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.transactionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetTodaySummary() = runBlocking {
        // Given
        val today = LocalDate.now().toString()
        dao.insert(createTransaction(type = "income", amount = 100_000, date = today))
        dao.insert(createTransaction(type = "income", amount = 50_000, date = today))
        dao.insert(createTransaction(type = "expense", amount = 30_000, date = today))

        // When
        val summary = dao.getTodaySummary(today)

        // Then
        assertThat(summary.totalIncome).isEqualTo(150_000)
        assertThat(summary.totalExpense).isEqualTo(30_000)
        assertThat(summary.transactionCount).isEqualTo(3)
    }

    @Test
    fun softDeleteShouldExcludeFromSummary() = runBlocking {
        val today = LocalDate.now().toString()
        val tx = createTransaction(type = "income", amount = 100_000, date = today)
        dao.insert(tx)
        dao.softDelete(tx.id)

        val summary = dao.getTodaySummary(today)
        assertThat(summary.totalIncome).isEqualTo(0)
    }

    @Test
    fun getBudgetSpentTodayShouldOnlyCountBudgetCategories() = runBlocking {
        val today = LocalDate.now().toString()
        dao.insert(createTransaction(type = "expense", amount = 20_000, date = today, category = "bbm"))
        dao.insert(createTransaction(type = "expense", amount = 15_000, date = today, category = "makan"))
        dao.insert(createTransaction(type = "expense", amount = 50_000, date = today, category = "service"))  // NOT a budget category

        val spent = dao.getBudgetSpentToday(today)

        assertThat(spent).hasSize(2)
        assertThat(spent.find { it.category == "bbm" }?.totalSpent).isEqualTo(20_000)
        assertThat(spent.find { it.category == "makan" }?.totalSpent).isEqualTo(15_000)
    }

    // Helper
    private fun createTransaction(
        type: String,
        amount: Long,
        date: String,
        category: String = "order",
    ) = TransactionEntity(
        id = UUID.randomUUID().toString(),
        createdAt = "${date}T12:00:00+07:00",
        type = type,
        amount = amount,
        category = category,
    )
}
```

### 4.5 Compose UI Test

```kotlin
// file: androidTest/.../feature/input/QuickInputScreenTest.kt
@HiltAndroidTest
class QuickInputScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun quickInputFlow_saveIncome() {
        composeRule.apply {
            // Navigate to input tab
            onNodeWithText("Input").performClick()

            // Verify income is selected by default
            onNodeWithText("Pemasukan").assertIsSelected()

            // Select category
            onNodeWithText("Order").performClick()

            // Enter amount via number pad
            onNodeWithText("5").performClick()
            onNodeWithText("0").performClick()
            onNodeWithText("000").performClick()

            // Verify display
            onNodeWithText("50.000").assertIsDisplayed()

            // Save
            onNodeWithText("Simpan").performClick()

            // Verify snackbar
            onNodeWithText("Pemasukan Rp 50.000 tersimpan").assertIsDisplayed()

            // Verify reset
            onNodeWithText("0").assertIsDisplayed()  // Amount reset
        }
    }

    @Test
    fun quickInputFlow_presetIsAdditive() {
        composeRule.apply {
            onNodeWithText("Input").performClick()

            // Enter base amount
            onNodeWithText("1").performClick()
            onNodeWithText("0").performClick()
            onNodeWithText("000").performClick()  // 10.000

            // Add preset
            onNodeWithText("+5rb").performClick()  // 10.000 + 5.000 = 15.000

            onNodeWithText("15.000").assertIsDisplayed()
        }
    }
}
```

### 4.6 Test Coverage Requirements

| Layer | Target | Tool | Priority |
|-------|--------|------|----------|
| **UseCase** | 90%+ | JUnit5 + MockK | 🟥 Critical |
| **ViewModel** | 85%+ | JUnit5 + MockK + Turbine | 🟥 Critical |
| **DAO** | 80%+ | Room in-memory + AndroidJUnit | 🟧 High |
| **Mapper/Util** | 95%+ | JUnit5 | 🟧 High |
| **Composable** | Critical paths | Compose Testing | 🟨 Medium |
| **Screenshot** | Visual regression | Paparazzi | 🟨 Medium |

---

## 5. Coding Standards

### 5.1 Naming Conventions

| Type | Convention | Contoh |
|------|-----------|--------|
| Package | `lowercase` | `feature.dashboard.ui` |
| Class/Interface | `PascalCase` | `DashboardViewModel` |
| Function | `camelCase` | `calculateDailyTarget()` |
| Constant | `UPPER_SNAKE` | `MAX_AMOUNT` |
| File | Match class name | `DashboardViewModel.kt` |
| Composable | `PascalCase` (noun) | `ProfitHeroCard` |
| UiState | `FeatureNameUiState` | `DashboardUiState` |
| UiAction | `FeatureNameUiAction` | `QuickInputUiAction` |
| DAO | `EntityNameDao` | `TransactionDao` |
| Entity | `EntityNameEntity` | `TransactionEntity` |
| UseCase | `VerbNounUseCase` | `SaveTransactionUseCase` |
| Repository | `NounRepository` | `TransactionRepository` |
| Repository impl | `NounRepositoryImpl` | `TransactionRepositoryImpl` |

### 5.2 File Organization Rules

```
Each feature package follows this structure:

feature/<name>/
  ├─ data/           (if feature has own data, otherwise uses shared)
  │   ├─ dao/
  │   ├─ entity/
  │   └─ RepositoryImpl.kt
  ├─ domain/
  │   ├─ model/       (feature-specific domain models)
  │   ├─ Repository.kt (interface)
  │   └─ XxxUseCase.kt
  └─ ui/
      ├─ XxxScreen.kt
      ├─ XxxViewModel.kt
      ├─ XxxUiState.kt
      ├─ XxxUiAction.kt   (optional, can use sealed class in UiState file)
      └─ component/
          └─ XxxComponent.kt
```

### 5.3 Composable Rules

| Rule | Detail |
|------|--------|
| Stateless | Composable hanya menerima data dan callback, tidak membuat state sendiri |
| Modifier first optional | `modifier: Modifier = Modifier` selalu jadi parameter terakhir |
| Max 50 lines | Jika lebih, extract ke composable baru |
| MaterialTheme tokens | Gunakan `MaterialTheme.colorScheme.*`, BUKAN hardcode `Color(0xFF...)` |
| Content description | Setiap `Icon()` harus punya `contentDescription` |
| Touch target | Minimum 48dp × 48dp |
| Preview | Setiap shared component punya `@Preview` |

### 5.4 Kotlin Style

```kotlin
// ✅ Prefer expression body for simple functions
fun TodaySummary.profit(): Long = totalIncome - totalExpense

// ✅ Use data class for simple state holders
@Immutable
data class BudgetInfo(val totalBudget: Long, val totalSpent: Long)

// ✅ Sealed interface for UiState (not sealed class)
sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val data: DashboardData) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

// ✅ operator fun invoke for UseCase
class SaveTransactionUseCase @Inject constructor(...) {
    suspend operator fun invoke(type: TransactionType, ...) { ... }
}

// ✅ Extension functions for mappers
fun TransactionEntity.toDomain(): Transaction = ...

// ❌ JANGAN:
// - var di data class (use val only)
// - !! operator (use safe calls or require())
// - hardcoded strings (use string resources)
// - Thread.sleep() (use delay())
// - GlobalScope (use viewModelScope)
```

---

## 6. Error Handling Strategy

### ViewModel Pattern

```kotlin
// Standard error handling in ViewModel
private fun loadData() {
    viewModelScope.launch {
        _uiState.value = XxxUiState.Loading
        try {
            val data = useCase()
            _uiState.value = XxxUiState.Success(data)
        } catch (e: Exception) {
            _uiState.value = XxxUiState.Error(
                message = e.localizedMessage ?: "Terjadi kesalahan"
            )
        }
    }
}

// For write operations (show snackbar instead of error screen)
private fun saveData() {
    viewModelScope.launch {
        try {
            useCase.save(...)
            _uiEvent.send(GlobalUiEvent.ShowSnackbar("Berhasil disimpan"))
        } catch (e: Exception) {
            _uiEvent.send(GlobalUiEvent.ShowSnackbar("Gagal: ${e.message}"))
        }
    }
}
```

### Error Messages (Bahasa Indonesia)

| Scenario | Message |
|----------|--------|
| Load failed | "Gagal memuat data" |
| Save success | "Berhasil disimpan" |
| Save failed | "Gagal menyimpan: {detail}" |
| Delete success | "Berhasil dihapus" |
| Network error | N/A (offline app) |
| Validation | Specific per field (lihat AD_04 Section 9) |
| Amount = 0 | Button disabled (no error message) |
| No category | Button disabled (no error message) |

---

## 7. Accessibility Implementation

### Content Descriptions

```kotlin
// ✅ Meaningful descriptions in Bahasa Indonesia
Icon(
    imageVector = Icons.Filled.Home,
    contentDescription = "Beranda",
)

// ✅ Semantic grouping for cards
Card(
    modifier = Modifier.semantics(mergeDescendants = true) {
        contentDescription = "Keuntungan hari ini Rp ${CurrencyFormatter.format(profit)}"
    }
) { ... }

// ✅ Action descriptions
IconButton(
    onClick = onDelete,
    modifier = Modifier.semantics {
        contentDescription = "Hapus ${item.name}"
    }
) { ... }

// ✅ Progress description
LinearProgressIndicator(
    progress = { percentage },
    modifier = Modifier.semantics {
        contentDescription = "Progress cicilan ${(percentage * 100).toInt()} persen"
    }
)
```

### Touch Targets

```kotlin
// ✅ Ensure 48dp minimum
IconButton(
    onClick = onClick,
    modifier = Modifier.minimumInteractiveComponentSize(),
) { ... }
```

---

## 8. Edge Cases & Pitfalls

### ⚠️ Watch Out

| Pitfall | Solution |
|---------|----------|
| **Timezone** | Selalu gunakan `ZoneId.of("Asia/Jakarta")` untuk semua operasi tanggal. JANGAN gunakan `LocalDate.now()` tanpa timezone |
| **Amount overflow** | Long max = 9.2 quintillion. Aman untuk Rupiah. Tapi cap input di 999.999.999 |
| **Due day > month length** | Feb hanya 28/29 hari. Gunakan `minOf(dueDay, month.lengthOfMonth())` |
| **Division by zero** | `workDays` bisa 0 jika semua hari libur. Selalu `coerceAtLeast(1)` |
| **Empty state** | Setiap list harus handle empty state (ilustrasi + pesan) |
| **Soft delete** | SEMUA query harus include `AND is_deleted = 0` |
| **Recomposition** | Jangan pass unstable lambda ke Composable. Gunakan `remember` atau method reference |
| **LazyColumn tanpa key** | Selalu sediakan `key = { it.id }` untuk performance |
| **Room thread** | Room queries di main thread = crash. Selalu `suspend` atau `Flow` |
| **StateFlow initial** | `collectAsStateWithLifecycle()` membutuhkan initial value. Gunakan `Loading` state |
| **Channel leak** | `Channel.BUFFERED` + `receiveAsFlow()` untuk one-shot events. JANGAN `SharedFlow(replay=1)` |
| **Back press** | `backStack.removeLastOrNull()` bisa return null jika sudah di root. Handle gracefully |
| **CSV encoding** | Gunakan `Charsets.UTF_8`. Sanitize commas dan newlines dalam note |
| **Date boundary** | Transaksi jam 23:59 WIB vs 00:01 WIB bisa beda hari. Selalu gunakan `date(created_at)` di SQL |

---

## 9. 📋 Complete File Checklist

Checklist ini bisa digunakan untuk tracking progress implementasi:

### Core
- [ ] `DriverWalletApp.kt`
- [ ] `MainActivity.kt`
- [ ] `core/database/AppDatabase.kt`
- [ ] `core/database/Converters.kt`
- [ ] `core/database/DatabaseModule.kt`
- [ ] `core/database/DatabaseCallback.kt`
- [ ] `core/datastore/AppDataStore.kt`
- [ ] `core/datastore/DataStoreModule.kt`
- [ ] `core/di/AppModule.kt`
- [ ] `core/di/RepositoryModule.kt`
- [ ] `core/model/TransactionType.kt`
- [ ] `core/model/Category.kt`
- [ ] `core/model/UrgencyLevel.kt`
- [ ] `core/model/DateTimeExt.kt`
- [ ] `core/ui/theme/Theme.kt`
- [ ] `core/ui/theme/Color.kt`
- [ ] `core/ui/theme/Type.kt`
- [ ] `core/ui/theme/Shape.kt`
- [ ] `core/ui/component/HeroCard.kt`
- [ ] `core/ui/component/CategoryIcon.kt`
- [ ] `core/ui/component/ProgressBar.kt`
- [ ] `core/ui/component/AmountText.kt`
- [ ] `core/ui/component/EmptyState.kt`
- [ ] `core/ui/component/LoadingIndicator.kt`
- [ ] `core/ui/navigation/Routes.kt`
- [ ] `core/ui/navigation/AppNavigation.kt`
- [ ] `core/ui/navigation/BottomNavBar.kt`
- [ ] `core/ui/navigation/GlobalUiEvent.kt`
- [ ] `core/util/CurrencyFormatter.kt`
- [ ] `core/util/DateFormatter.kt`
- [ ] `core/util/UuidGenerator.kt`

### Shared Data
- [ ] `shared/data/entity/TransactionEntity.kt`
- [ ] `shared/data/dao/TransactionDao.kt`
- [ ] `shared/data/TransactionRepositoryImpl.kt`
- [ ] `shared/data/repository/TransactionRepository.kt`
- [ ] `shared/data/mapper/TransactionMapper.kt`

### Feature: Dashboard
- [ ] `feature/dashboard/domain/GetDashboardSummaryUseCase.kt`
- [ ] `feature/dashboard/domain/CalculateDailyTargetUseCase.kt`
- [ ] `feature/dashboard/domain/model/DashboardData.kt`
- [ ] `feature/dashboard/domain/model/TodaySummary.kt`
- [ ] `feature/dashboard/domain/model/DailyTarget.kt`
- [ ] `feature/dashboard/domain/model/BudgetInfo.kt`
- [ ] `feature/dashboard/domain/model/DueAlert.kt`
- [ ] `feature/dashboard/ui/DashboardScreen.kt`
- [ ] `feature/dashboard/ui/DashboardViewModel.kt`
- [ ] `feature/dashboard/ui/DashboardUiState.kt`
- [ ] `feature/dashboard/ui/component/ProfitHeroCard.kt`
- [ ] `feature/dashboard/ui/component/IncomeExpenseRow.kt`
- [ ] `feature/dashboard/ui/component/DailyTargetSection.kt`
- [ ] `feature/dashboard/ui/component/BudgetRemainingCard.kt`
- [ ] `feature/dashboard/ui/component/DueAlertCard.kt`
- [ ] `feature/dashboard/ui/component/TodayTransactionList.kt`

### Feature: Quick Input
- [ ] `feature/input/domain/SaveTransactionUseCase.kt`
- [ ] `feature/input/ui/QuickInputScreen.kt`
- [ ] `feature/input/ui/QuickInputViewModel.kt`
- [ ] `feature/input/ui/QuickInputUiState.kt`
- [ ] `feature/input/ui/QuickInputUiAction.kt`
- [ ] `feature/input/ui/component/TypeToggle.kt`
- [ ] `feature/input/ui/component/CategoryGrid.kt`
- [ ] `feature/input/ui/component/AmountDisplay.kt`
- [ ] `feature/input/ui/component/NoteInput.kt`
- [ ] `feature/input/ui/component/PresetButtons.kt`
- [ ] `feature/input/ui/component/NumberPad.kt`
- [ ] `feature/input/ui/component/SaveButton.kt`

### Feature: Debt
- [ ] `feature/debt/data/entity/DebtEntity.kt`
- [ ] `feature/debt/data/entity/DebtScheduleEntity.kt`
- [ ] `feature/debt/data/dao/DebtDao.kt`
- [ ] `feature/debt/data/dao/DebtScheduleDao.kt`
- [ ] `feature/debt/data/DebtRepositoryImpl.kt`
- [ ] `feature/debt/domain/DebtRepository.kt`
- [ ] `feature/debt/domain/GetActiveDebtsUseCase.kt`
- [ ] `feature/debt/domain/SaveDebtUseCase.kt`
- [ ] `feature/debt/domain/PayDebtInstallmentUseCase.kt`
- [ ] `feature/debt/domain/model/Debt.kt`
- [ ] `feature/debt/domain/model/DebtSchedule.kt`
- [ ] `feature/debt/domain/model/DebtWithSchedule.kt`
- [ ] `feature/debt/ui/list/DebtListScreen.kt`
- [ ] `feature/debt/ui/list/DebtListViewModel.kt`
- [ ] `feature/debt/ui/list/DebtListUiState.kt`
- [ ] `feature/debt/ui/list/component/DebtHeroCard.kt`
- [ ] `feature/debt/ui/list/component/DebtCardItem.kt`
- [ ] `feature/debt/ui/list/component/PaymentBottomSheet.kt`
- [ ] `feature/debt/ui/form/DebtFormScreen.kt`
- [ ] `feature/debt/ui/form/DebtFormViewModel.kt`
- [ ] `feature/debt/ui/form/DebtFormUiState.kt`

### Feature: Report
- [ ] `feature/report/domain/GetWeeklyReportUseCase.kt`
- [ ] `feature/report/domain/GetMonthlyReportUseCase.kt`
- [ ] `feature/report/domain/GetCustomReportUseCase.kt`
- [ ] `feature/report/domain/ExportCsvUseCase.kt`
- [ ] `feature/report/domain/model/WeeklyReport.kt`
- [ ] `feature/report/domain/model/MonthlyReport.kt`
- [ ] `feature/report/domain/model/DailySummary.kt`
- [ ] `feature/report/domain/model/CategorySummary.kt`
- [ ] `feature/report/ui/ReportScreen.kt`
- [ ] `feature/report/ui/ReportViewModel.kt`
- [ ] `feature/report/ui/ReportUiState.kt`
- [ ] `feature/report/ui/component/ReportTabRow.kt`
- [ ] `feature/report/ui/component/WeekNavigator.kt`
- [ ] `feature/report/ui/component/BarChartView.kt`
- [ ] `feature/report/ui/component/SummaryCards.kt`
- [ ] `feature/report/ui/component/ProfitHeroCard.kt`
- [ ] `feature/report/ui/component/DailyDetailList.kt`
- [ ] `feature/report/ui/component/CategoryBreakdownList.kt`

### Feature: Settings
- [ ] `feature/settings/data/entity/DailyBudgetEntity.kt`
- [ ] `feature/settings/data/entity/MonthlyExpenseEntity.kt`
- [ ] `feature/settings/data/entity/DailyExpenseEntity.kt`
- [ ] `feature/settings/data/entity/SettingsEntity.kt`
- [ ] `feature/settings/data/dao/DailyBudgetDao.kt`
- [ ] `feature/settings/data/dao/MonthlyExpenseDao.kt`
- [ ] `feature/settings/data/dao/DailyExpenseDao.kt`
- [ ] `feature/settings/data/dao/SettingsDao.kt`
- [ ] `feature/settings/data/SettingsRepositoryImpl.kt`
- [ ] `feature/settings/domain/SettingsRepository.kt`
- [ ] `feature/settings/domain/SaveDailyBudgetsUseCase.kt`
- [ ] `feature/settings/domain/SaveMonthlyExpenseUseCase.kt`
- [ ] `feature/settings/domain/SaveDailyExpenseUseCase.kt`
- [ ] `feature/settings/domain/model/DailyBudget.kt`
- [ ] `feature/settings/domain/model/MonthlyExpense.kt`
- [ ] `feature/settings/domain/model/DailyExpense.kt`
- [ ] `feature/settings/ui/SettingsScreen.kt`
- [ ] `feature/settings/ui/SettingsViewModel.kt`
- [ ] `feature/settings/ui/SettingsUiState.kt`
- [ ] `feature/settings/ui/component/DarkModeToggle.kt`
- [ ] `feature/settings/ui/component/BudgetSection.kt`
- [ ] `feature/settings/ui/component/TargetDateRow.kt`
- [ ] `feature/settings/ui/component/FixedExpenseSection.kt`
- [ ] `feature/settings/ui/component/ExpenseFormDialog.kt`

### Feature: Onboarding
- [ ] `feature/onboarding/ui/OnboardingOverlay.kt`
- [ ] `feature/onboarding/ui/OnboardingPage.kt`

### Config
- [ ] `gradle/libs.versions.toml`
- [ ] `build.gradle.kts` (root)
- [ ] `app/build.gradle.kts`
- [ ] `settings.gradle.kts`
- [ ] `gradle.properties`
- [ ] `res/xml/file_paths.xml`
- [ ] `res/values/strings.xml`
- [ ] `AndroidManifest.xml`

### Tests
- [ ] `test/.../QuickInputViewModelTest.kt`
- [ ] `test/.../DashboardViewModelTest.kt`
- [ ] `test/.../DebtListViewModelTest.kt`
- [ ] `test/.../PayDebtInstallmentUseCaseTest.kt`
- [ ] `test/.../ReportViewModelTest.kt`
- [ ] `test/.../CurrencyFormatterTest.kt`
- [ ] `androidTest/.../TransactionDaoTest.kt`
- [ ] `androidTest/.../DebtDaoTest.kt`
- [ ] `androidTest/.../MigrationTest.kt`
- [ ] `androidTest/.../QuickInputScreenTest.kt`

**Total: ~120 files**

---

*Ini adalah dokumen terakhir dari seri Architecture Decision (AD_01 – AD_05). Dengan kelima dokumen ini, AI Builder memiliki semua informasi yang diperlukan untuk membangun Driver Wallet dari nol.*
