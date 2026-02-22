# AD_03: Technical Architecture — Driver Wallet Android

> **Audience:** AI Builder & Developer
> **Prerequisite:** Baca [AD_01_PRD_Product.md](./AD_01_PRD_Product.md) dan [AD_02_User_Stories.md](./AD_02_User_Stories.md)

---

## 1. Technology Stack (2026 Mandatory)

| Layer | Technology | Version | Notes |
|-------|-----------|---------|-------|
| **Language** | Kotlin | 2.3.x | KSP only (BUKAN KAPT) |
| **UI Framework** | Jetpack Compose | 1.10.x | compose.material3 ONLY (BUKAN Material 2) |
| **Design System** | Material 3 Expressive | Latest | Dynamic Color (Material You) |
| **Navigation** | Navigation 3 | Latest | Type-safe, user-owned backstack (BUKAN Nav 2) |
| **Architecture** | MVI + Clean Architecture | — | UI → ViewModel → UseCase → Repository → DataSource |
| **State Management** | StateFlow + UiState sealed interface | — | BUKAN LiveData |
| **DI** | Hilt | Latest | Primary DI framework |
| **Serialization** | kotlinx.serialization | Latest | BUKAN Gson |
| **HTTP Client** | — | — | Tidak digunakan (offline-only app) |
| **Database** | Room + KSP | Latest | Local-only, SQLite |
| **Preferences** | DataStore | Latest | BUKAN SharedPreferences |
| **Image Loading** | Coil 3 | Latest | BUKAN Glide/Picasso |
| **Async** | Coroutines + Flow | Latest | Semua async work |
| **Annotation Processing** | KSP | Latest | BUKAN KAPT |
| **Min SDK** | 26 | — | Android 8.0 Oreo |
| **Target SDK** | 36 | — | Latest |
| **Compile SDK** | 36 | — | |
| **Build System** | Gradle + Kotlin DSL | Latest | Version Catalogs (libs.versions.toml) |

### ⛔ Anti-Pattern Checklist (JANGAN Gunakan)

| ❌ JANGAN | ✅ GUNAKAN |
|-----------|------------|
| KAPT | KSP |
| Gson | kotlinx.serialization |
| Material 2 (`androidx.compose.material`) | Material 3 (`androidx.compose.material3`) |
| Navigation 2 (`NavHost`, `NavController`) | Navigation 3 (`NavDisplay`, user-owned backstack) |
| LiveData | StateFlow |
| SharedPreferences | DataStore |
| Glide / Picasso | Coil 3 |
| Retrofit | Tidak perlu (offline app) |
| RxJava | Coroutines + Flow |
| `mutableStateOf` untuk complex state | `MutableStateFlow` + `UiState` sealed interface |
| Fragment | Compose-only (no fragments) |

---

## 2. Architecture Overview

### Layer Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                      │
│                                                              │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐   │
│  │  Composable   │    │  Composable   │    │  Composable   │   │
│  │  (Stateless)  │    │  (Stateless)  │    │  (Stateless)  │   │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘   │
│         │                   │                   │            │
│         └───────────────────┼───────────────────┘            │
│                             │                                │
│                    ┌────────▼────────┐                       │
│                    │   ViewModel     │                       │
│                    │  (Hilt inject)  │                       │
│                    │                 │                       │
│                    │  UiState  ←──── StateFlow               │
│                    │  UiAction ────→ process()               │
│                    │  UiEvent  ←──── Channel (one-shot)      │
│                    └────────┬────────┘                       │
│                             │                                │
├─────────────────────────────┼────────────────────────────────┤
│                      DOMAIN LAYER                            │
│                             │                                │
│                    ┌────────▼────────┐                       │
│                    │    UseCase      │                       │
│                    │  (single resp.) │                       │
│                    │                 │                       │
│                    │  operator fun   │                       │
│                    │  invoke(...)    │                       │
│                    └────────┬────────┘                       │
│                             │                                │
│                    ┌────────▼────────┐                       │
│                    │  Repository     │                       │
│                    │  (interface)    │                       │
│                    └────────┬────────┘                       │
│                             │                                │
├─────────────────────────────┼────────────────────────────────┤
│                       DATA LAYER                             │
│                             │                                │
│                    ┌────────▼────────┐                       │
│                    │  RepositoryImpl │                       │
│                    │                 │                       │
│                    └──┬──────────┬───┘                       │
│                       │          │                           │
│              ┌────────▼──┐  ┌───▼─────────┐                 │
│              │  Room DAO  │  │  DataStore   │                │
│              │ (SQLite)   │  │ (Prefs/Proto)│                │
│              └────────────┘  └──────────────┘                │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### Layer Rules

| Layer | Aturan |
|-------|--------|
| **Presentation** | Composable functions stateless + state hoisting. ViewModel holds UiState via StateFlow. Tidak boleh akses Room/DataStore langsung |
| **Domain** | UseCase = pure business logic. 1 UseCase = 1 tanggung jawab. Menerima Repository interface (bukan impl). Tidak boleh import Android framework |
| **Data** | RepositoryImpl mengimplementasi Repository interface. Mengkoordinasi Room DAO + DataStore. Room Entity mapping ke Domain model |

### Data Flow (MVI Pattern)

```
User Interaction
      │
      ▼
  UiAction (sealed interface)
      │
      ▼
  ViewModel.onAction(action)
      │
      ▼
  UseCase.invoke(params)
      │
      ▼
  Repository.method(params)
      │
      ▼
  Room DAO / DataStore
      │
      ▼
  Result / Flow<Data>
      │
      ▼
  ViewModel updates _uiState: MutableStateFlow<UiState>
      │
      ▼
  Composable collects uiState via collectAsStateWithLifecycle()
      │
      ▼
  UI Re-renders
```

---

## 3. Project Structure

### Single-Module Architecture

Karena ini app sederhana (5 screen, offline-only), gunakan **single module** dengan package-by-feature:

```
app/
├── build.gradle.kts
├── src/
│   ├── main/
│   │   ├── java/com/driverwallet/app/
│   │   │   │
│   │   │   ├── DriverWalletApp.kt              // @HiltAndroidApp Application class
│   │   │   ├── MainActivity.kt                  // @AndroidEntryPoint, single Activity
│   │   │   │
│   │   │   ├── core/                            // === SHARED / CORE ===
│   │   │   │   ├── database/
│   │   │   │   │   ├── AppDatabase.kt           // @Database, Room database definition
│   │   │   │   │   ├── Converters.kt            // TypeConverters (LocalDate, etc.)
│   │   │   │   │   └── DatabaseModule.kt         // @Module @InstallIn(SingletonComponent)
│   │   │   │   │
│   │   │   │   ├── datastore/
│   │   │   │   │   ├── AppDataStore.kt           // DataStore wrapper
│   │   │   │   │   └── DataStoreModule.kt        // @Module
│   │   │   │   │
│   │   │   │   ├── di/
│   │   │   │   │   └── AppModule.kt              // App-level bindings
│   │   │   │   │
│   │   │   │   ├── model/
│   │   │   │   │   ├── TransactionType.kt        // enum: INCOME, EXPENSE
│   │   │   │   │   ├── Category.kt               // data class + predefined lists
│   │   │   │   │   ├── UrgencyLevel.kt           // enum: OVERDUE, CRITICAL, WARNING, NORMAL
│   │   │   │   │   └── DateTimeExt.kt            // nowJakarta(), toIsoString(), etc.
│   │   │   │   │
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Theme.kt              // DriverWalletTheme, Dynamic Color
│   │   │   │   │   │   ├── Color.kt              // M3 color tokens (fallback)
│   │   │   │   │   │   ├── Type.kt               // Typography scale
│   │   │   │   │   │   └── Shape.kt              // Shape scale
│   │   │   │   │   │
│   │   │   │   │   ├── component/                // Shared composables
│   │   │   │   │   │   ├── HeroCard.kt
│   │   │   │   │   │   ├── CategoryIcon.kt
│   │   │   │   │   │   ├── ProgressBar.kt
│   │   │   │   │   │   ├── AmountText.kt         // Formatted Rp display
│   │   │   │   │   │   ├── EmptyState.kt
│   │   │   │   │   │   └── LoadingIndicator.kt
│   │   │   │   │   │
│   │   │   │   │   └── navigation/
│   │   │   │   │       ├── AppNavigation.kt       // NavDisplay + route mapping
│   │   │   │   │       ├── Routes.kt              // All @Serializable route objects
│   │   │   │   │       └── BottomNavBar.kt         // NavigationBar composable
│   │   │   │   │
│   │   │   │   └── util/
│   │   │   │       ├── CurrencyFormatter.kt       // Long → "150.000", "150rb"
│   │   │   │       ├── DateFormatter.kt           // LocalDate → "25 Okt 2024"
│   │   │   │       └── UuidGenerator.kt           // UUID wrapper
│   │   │   │
│   │   │   ├── feature/                          // === FEATURES ===
│   │   │   │   │
│   │   │   │   ├── dashboard/                    // F01
│   │   │   │   │   ├── data/
│   │   │   │   │   │   └── (uses shared repositories)
│   │   │   │   │   ├── domain/
│   │   │   │   │   │   ├── GetDashboardSummaryUseCase.kt
│   │   │   │   │   │   ├── CalculateDailyTargetUseCase.kt
│   │   │   │   │   │   └── model/
│   │   │   │   │   │       ├── DashboardData.kt
│   │   │   │   │   │       ├── TodaySummary.kt
│   │   │   │   │   │       ├── DailyTarget.kt
│   │   │   │   │   │       ├── BudgetInfo.kt
│   │   │   │   │   │       └── DueAlert.kt
│   │   │   │   │   └── ui/
│   │   │   │   │       ├── DashboardScreen.kt
│   │   │   │   │       ├── DashboardViewModel.kt
│   │   │   │   │       ├── DashboardUiState.kt
│   │   │   │   │       └── component/
│   │   │   │   │           ├── ProfitHeroCard.kt
│   │   │   │   │           ├── IncomeExpenseRow.kt
│   │   │   │   │           ├── DailyTargetSection.kt
│   │   │   │   │           ├── BudgetRemainingCard.kt
│   │   │   │   │           ├── DueAlertCard.kt
│   │   │   │   │           └── TodayTransactionList.kt
│   │   │   │   │
│   │   │   │   ├── input/                        // F02
│   │   │   │   │   ├── domain/
│   │   │   │   │   │   └── SaveTransactionUseCase.kt
│   │   │   │   │   └── ui/
│   │   │   │   │       ├── QuickInputScreen.kt
│   │   │   │   │       ├── QuickInputViewModel.kt
│   │   │   │   │       ├── QuickInputUiState.kt
│   │   │   │   │       ├── QuickInputUiAction.kt
│   │   │   │   │       ├── QuickInputUiEvent.kt
│   │   │   │   │       └── component/
│   │   │   │   │           ├── TypeToggle.kt
│   │   │   │   │           ├── CategoryGrid.kt
│   │   │   │   │           ├── AmountDisplay.kt
│   │   │   │   │           ├── NoteInput.kt
│   │   │   │   │           ├── PresetButtons.kt
│   │   │   │   │           ├── NumberPad.kt
│   │   │   │   │           └── SaveButton.kt
│   │   │   │   │
│   │   │   │   ├── debt/                         // F05
│   │   │   │   │   ├── data/
│   │   │   │   │   │   ├── DebtRepositoryImpl.kt
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── DebtDao.kt
│   │   │   │   │   │   │   └── DebtScheduleDao.kt
│   │   │   │   │   │   └── entity/
│   │   │   │   │   │       ├── DebtEntity.kt
│   │   │   │   │   │       └── DebtScheduleEntity.kt
│   │   │   │   │   ├── domain/
│   │   │   │   │   │   ├── DebtRepository.kt         // interface
│   │   │   │   │   │   ├── GetActiveDebtsUseCase.kt
│   │   │   │   │   │   ├── SaveDebtUseCase.kt
│   │   │   │   │   │   ├── PayDebtInstallmentUseCase.kt
│   │   │   │   │   │   └── model/
│   │   │   │   │   │       ├── Debt.kt                // domain model
│   │   │   │   │   │       ├── DebtSchedule.kt
│   │   │   │   │   │       └── DebtWithSchedule.kt
│   │   │   │   │   └── ui/
│   │   │   │   │       ├── list/
│   │   │   │   │       │   ├── DebtListScreen.kt
│   │   │   │   │       │   ├── DebtListViewModel.kt
│   │   │   │   │       │   ├── DebtListUiState.kt
│   │   │   │   │       │   └── component/
│   │   │   │   │       │       ├── DebtHeroCard.kt
│   │   │   │   │       │       ├── DebtCardItem.kt
│   │   │   │   │       │       └── PaymentBottomSheet.kt
│   │   │   │   │       └── form/
│   │   │   │   │           ├── DebtFormScreen.kt
│   │   │   │   │           ├── DebtFormViewModel.kt
│   │   │   │   │           └── DebtFormUiState.kt
│   │   │   │   │
│   │   │   │   ├── report/                       // F06
│   │   │   │   │   ├── domain/
│   │   │   │   │   │   ├── GetWeeklyReportUseCase.kt
│   │   │   │   │   │   ├── GetMonthlyReportUseCase.kt
│   │   │   │   │   │   ├── GetCustomReportUseCase.kt
│   │   │   │   │   │   ├── ExportCsvUseCase.kt
│   │   │   │   │   │   └── model/
│   │   │   │   │   │       ├── WeeklyReport.kt
│   │   │   │   │   │       ├── MonthlyReport.kt
│   │   │   │   │   │       ├── DailySummary.kt
│   │   │   │   │   │       └── CategorySummary.kt
│   │   │   │   │   └── ui/
│   │   │   │   │       ├── ReportScreen.kt
│   │   │   │   │       ├── ReportViewModel.kt
│   │   │   │   │       ├── ReportUiState.kt
│   │   │   │   │       └── component/
│   │   │   │   │           ├── ReportTabRow.kt
│   │   │   │   │           ├── WeekNavigator.kt
│   │   │   │   │           ├── BarChartView.kt
│   │   │   │   │           ├── SummaryCards.kt
│   │   │   │   │           ├── ProfitHeroCard.kt
│   │   │   │   │           ├── DailyDetailList.kt
│   │   │   │   │           └── CategoryBreakdownList.kt
│   │   │   │   │
│   │   │   │   ├── settings/                     // F07
│   │   │   │   │   ├── data/
│   │   │   │   │   │   ├── SettingsRepositoryImpl.kt
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── DailyBudgetDao.kt
│   │   │   │   │   │   │   ├── MonthlyExpenseDao.kt
│   │   │   │   │   │   │   ├── DailyExpenseDao.kt
│   │   │   │   │   │   │   └── SettingsDao.kt
│   │   │   │   │   │   └── entity/
│   │   │   │   │   │       ├── DailyBudgetEntity.kt
│   │   │   │   │   │       ├── MonthlyExpenseEntity.kt
│   │   │   │   │   │       ├── DailyExpenseEntity.kt
│   │   │   │   │   │       └── SettingsEntity.kt
│   │   │   │   │   ├── domain/
│   │   │   │   │   │   ├── SettingsRepository.kt      // interface
│   │   │   │   │   │   ├── SaveDailyBudgetsUseCase.kt
│   │   │   │   │   │   ├── SaveMonthlyExpenseUseCase.kt
│   │   │   │   │   │   ├── SaveDailyExpenseUseCase.kt
│   │   │   │   │   │   └── model/
│   │   │   │   │   │       ├── DailyBudget.kt
│   │   │   │   │   │       ├── MonthlyExpense.kt
│   │   │   │   │   │       └── DailyExpense.kt
│   │   │   │   │   └── ui/
│   │   │   │   │       ├── SettingsScreen.kt
│   │   │   │   │       ├── SettingsViewModel.kt
│   │   │   │   │       ├── SettingsUiState.kt
│   │   │   │   │       └── component/
│   │   │   │   │           ├── DarkModeToggle.kt
│   │   │   │   │           ├── BudgetSection.kt
│   │   │   │   │           ├── TargetDateRow.kt
│   │   │   │   │           ├── FixedExpenseSection.kt
│   │   │   │   │           └── ExpenseFormDialog.kt
│   │   │   │   │
│   │   │   │   └── onboarding/                   // F08
│   │   │   │       └── ui/
│   │   │   │           ├── OnboardingOverlay.kt
│   │   │   │           └── OnboardingPage.kt
│   │   │   │
│   │   │   └── shared/                           // === SHARED DATA ===
│   │   │       └── data/
│   │   │           ├── TransactionRepositoryImpl.kt
│   │   │           ├── dao/
│   │   │           │   └── TransactionDao.kt
│   │   │           ├── entity/
│   │   │           │   └── TransactionEntity.kt
│   │   │           └── repository/
│   │   │               └── TransactionRepository.kt   // interface
│   │   │
│   │   ├── res/
│   │   │   ├── values/
│   │   │   │   ├── strings.xml                  // Bahasa Indonesia
│   │   │   │   ├── themes.xml                   // M3 theme fallback
│   │   │   │   └── colors.xml                   // Hanya jika perlu XML (minimal)
│   │   │   └── xml/
│   │   │       └── file_paths.xml               // FileProvider untuk CSV export
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   ├── test/                                     // Unit tests
│   │   └── java/com/driverwallet/app/
│   │       ├── feature/
│   │       │   ├── dashboard/
│   │       │   │   └── DashboardViewModelTest.kt
│   │       │   ├── input/
│   │       │   │   └── QuickInputViewModelTest.kt
│   │       │   ├── debt/
│   │       │   │   ├── DebtListViewModelTest.kt
│   │       │   │   └── PayDebtInstallmentUseCaseTest.kt
│   │       │   └── report/
│   │       │       └── ReportViewModelTest.kt
│   │       └── shared/
│   │           └── util/
│   │               └── CurrencyFormatterTest.kt
│   │
│   └── androidTest/                              // Instrumented tests
│       └── java/com/driverwallet/app/
│           ├── core/database/
│           │   ├── TransactionDaoTest.kt
│           │   ├── DebtDaoTest.kt
│           │   └── MigrationTest.kt
│           └── feature/
│               └── input/
│                   └── QuickInputScreenTest.kt
│
├── gradle/
│   └── libs.versions.toml                        // Version Catalog
│
├── build.gradle.kts                              // Root build
├── settings.gradle.kts
└── gradle.properties
```

---

## 4. Dependency Injection (Hilt)

### Application

```kotlin
@HiltAndroidApp
class DriverWalletApp : Application()
```

### MainActivity

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DriverWalletTheme {
                AppNavigation()
            }
        }
    }
}
```

### Database Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "driver_wallet.db"
    ).build()

    @Provides fun provideTransactionDao(db: AppDatabase) = db.transactionDao()
    @Provides fun provideDebtDao(db: AppDatabase) = db.debtDao()
    @Provides fun provideDebtScheduleDao(db: AppDatabase) = db.debtScheduleDao()
    @Provides fun provideDailyBudgetDao(db: AppDatabase) = db.dailyBudgetDao()
    @Provides fun provideMonthlyExpenseDao(db: AppDatabase) = db.monthlyExpenseDao()
    @Provides fun provideDailyExpenseDao(db: AppDatabase) = db.dailyExpenseDao()
    @Provides fun provideSettingsDao(db: AppDatabase) = db.settingsDao()
}
```

### Repository Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindDebtRepository(
        impl: DebtRepositoryImpl
    ): DebtRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
```

### ViewModel Injection

```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardSummary: GetDashboardSummaryUseCase,
    private val calculateDailyTarget: CalculateDailyTargetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<GlobalUiEvent>(Channel.BUFFERED)
    val uiEvent: Flow<GlobalUiEvent> = _uiEvent.receiveAsFlow()

    init {
        loadDashboard()
    }

    fun onAction(action: DashboardUiAction) {
        when (action) {
            is DashboardUiAction.Refresh -> loadDashboard()
            // ... other actions
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val today = LocalDate.now(ZoneId.of("Asia/Jakarta"))
                val data = getDashboardSummary(today)
                _uiState.value = DashboardUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

---

## 5. Navigation 3 Setup

### Routes Definition

```kotlin
// file: core/ui/navigation/Routes.kt
import kotlinx.serialization.Serializable

@Serializable data object DashboardRoute
@Serializable data object QuickInputRoute
@Serializable data object DebtListRoute
@Serializable data class DebtFormRoute(val debtId: String? = null)
@Serializable data object ReportRoute
@Serializable data object SettingsRoute
```

### App Navigation

```kotlin
// file: core/ui/navigation/AppNavigation.kt
@Composable
fun AppNavigation() {
    val backStack = rememberMutableStateListOf<Any>(DashboardRoute)
    val snackbarHostState = remember { SnackbarHostState() }

    // Determine current top-level route for bottom nav
    val currentRoute = backStack.lastOrNull()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    // Clear backstack to root, then add target
                    backStack.clear()
                    backStack.add(route)
                },
                hasDebtBadge = /* observe from shared state */
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding),
            entryProvider = entryProvider {
                entry<DashboardRoute> {
                    DashboardScreen(
                        onNavigate = { route -> backStack.add(route) }
                    )
                }
                entry<QuickInputRoute> {
                    QuickInputScreen(
                        snackbarHostState = snackbarHostState,
                        onNavigateBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<DebtListRoute> {
                    DebtListScreen(
                        onNavigateToForm = { debtId ->
                            backStack.add(DebtFormRoute(debtId))
                        }
                    )
                }
                entry<DebtFormRoute> { route ->
                    DebtFormScreen(
                        debtId = route.debtId,
                        snackbarHostState = snackbarHostState,
                        onNavigateBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<ReportRoute> {
                    ReportScreen()
                }
                entry<SettingsRoute> {
                    SettingsScreen(
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        )
    }
}
```

### Bottom Navigation Bar

```kotlin
// file: core/ui/navigation/BottomNavBar.kt
data class BottomNavItem(
    val route: Any,
    val label: String,
    val icon: ImageVector,          // outlined
    val selectedIcon: ImageVector,   // filled
    val hasBadge: Boolean = false,
)

@Composable
fun BottomNavBar(
    currentRoute: Any?,
    onNavigate: (Any) -> Unit,
    hasDebtBadge: Boolean,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        BottomNavItem(DashboardRoute, "Beranda", Icons.Outlined.Home, Icons.Filled.Home),
        BottomNavItem(QuickInputRoute, "Input", Icons.Outlined.AddCircle, Icons.Filled.AddCircle),
        BottomNavItem(DebtListRoute, "Hutang", Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet, hasBadge = hasDebtBadge),
        BottomNavItem(ReportRoute, "Laporan", Icons.Outlined.BarChart, Icons.Filled.BarChart),
        BottomNavItem(SettingsRoute, "Pengaturan", Icons.Outlined.Settings, Icons.Filled.Settings),
    )

    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            val isSelected = currentRoute?.let { it::class == item.route::class } ?: false
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    if (item.hasBadge) {
                        BadgedBox(badge = { Badge() }) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.icon,
                                contentDescription = item.label,
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.icon,
                            contentDescription = item.label,
                        )
                    }
                },
                label = { Text(item.label) },
            )
        }
    }
}
```

---

## 6. Room Database

### Database Definition

```kotlin
@Database(
    entities = [
        TransactionEntity::class,
        DebtEntity::class,
        DebtScheduleEntity::class,
        DailyBudgetEntity::class,
        MonthlyExpenseEntity::class,
        DailyExpenseEntity::class,
        SettingsEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun debtDao(): DebtDao
    abstract fun debtScheduleDao(): DebtScheduleDao
    abstract fun dailyBudgetDao(): DailyBudgetDao
    abstract fun monthlyExpenseDao(): MonthlyExpenseDao
    abstract fun dailyExpenseDao(): DailyExpenseDao
    abstract fun settingsDao(): SettingsDao
}
```

### Type Converters

```kotlin
class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val zonedFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.format(formatter)

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it, formatter) }

    @TypeConverter
    fun fromZonedDateTime(zdt: ZonedDateTime?): String? = zdt?.format(zonedFormatter)

    @TypeConverter
    fun toZonedDateTime(value: String?): ZonedDateTime? = value?.let { ZonedDateTime.parse(it, zonedFormatter) }
}
```

> **Detail Entity dan DAO ada di [AD_04_Data_Model.md](./AD_04_Data_Model.md)**

---

## 7. DataStore Setup

```kotlin
// file: core/datastore/AppDataStore.kt
class AppDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore by preferencesDataStore(name = "driver_wallet_prefs")

    companion object {
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        // Settings yang simple dan non-relational disimpan di DataStore
        // Settings yang relational (budgets, expenses) disimpan di Room
    }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[HAS_SEEN_ONBOARDING] ?: false }

    suspend fun setOnboardingSeen() {
        context.dataStore.edit { prefs ->
            prefs[HAS_SEEN_ONBOARDING] = true
        }
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[DARK_MODE] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE] = enabled
        }
    }
}
```

### DataStore vs Room — Kapan Memakai Apa?

| Data | Storage | Alasan |
|------|---------|--------|
| `has_seen_onboarding` | DataStore | Simple boolean flag, no relation |
| `dark_mode` | DataStore | Simple boolean preference |
| Daily budgets (BBM, Makan, etc.) | Room | Multiple rows, queryable, used in calculation |
| Monthly/Daily fixed expenses | Room | CRUD with soft delete, relational to categories |
| Debt target date | Room (Settings table) | Key-value but used in complex queries |
| Rest days config | Room (Settings table) | Used in DailyTarget calculation |
| Transactions | Room | Core data, queryable, aggregated |
| Debts & Schedules | Room | Relational data, complex queries |

---

## 8. Theme Setup (Material 3 + Dynamic Color)

```kotlin
// file: core/ui/theme/Theme.kt
@Composable
fun DriverWalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
            else dynamicLightColorScheme(LocalContext.current)
        }
        darkTheme -> darkColorScheme(
            primary = PurplePrimary,                  // #6750A4
            primaryContainer = PurplePrimaryContainer, // #EADDFF
            secondary = PurpleSecondary,               // #625B71
            secondaryContainer = PurpleSecondaryContainer, // #E8DEF8
            error = ErrorRed,                          // #B3261E
            errorContainer = ErrorContainer,            // #F9DEDC
            surface = DarkSurface,                     // #1C1B1F
            surfaceVariant = DarkSurfaceVariant,       // #49454F
            background = DarkBackground,               // #1C1B1F
        )
        else -> lightColorScheme(
            primary = PurplePrimary,
            primaryContainer = PurplePrimaryContainer,
            secondary = PurpleSecondary,
            secondaryContainer = PurpleSecondaryContainer,
            error = ErrorRed,
            errorContainer = ErrorContainer,
            surface = LightSurface,                    // #FFFBFE
            surfaceVariant = LightSurfaceVariant,      // #E7E0EC
            background = LightBackground,              // #FFFBFE
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DriverWalletTypography,
        shapes = DriverWalletShapes,
        content = content,
    )
}
```

### Typography

```kotlin
// file: core/ui/theme/Type.kt
val DriverWalletTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
    ),
    // Use default M3 typography scale
    // Custom: Large amount display (not in M3 scale)
)

// Custom text style for amount displays
val AmountDisplayStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 80.sp,
    lineHeight = 88.sp,
)

val HeroAmountStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 42.sp,
    lineHeight = 48.sp,
)
```

### Shapes

```kotlin
// file: core/ui/theme/Shape.kt
val DriverWalletShapes = Shapes(
    small = RoundedCornerShape(4.dp),     // M3 filled text field (top only)
    medium = RoundedCornerShape(16.dp),   // Standard cards
    large = RoundedCornerShape(28.dp),    // Hero cards, bottom sheets
)

// Custom shape tokens
object AppShapes {
    val HeroCard = RoundedCornerShape(28.dp)
    val StandardCard = RoundedCornerShape(16.dp)
    val DebtCard = RoundedCornerShape(24.dp)
    val CategoryIcon = RoundedCornerShape(20.dp)
    val Pill = RoundedCornerShape(50)   // CircleShape for buttons
    val FilledTextField = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
}
```

---

## 9. Composable Patterns & Rules

### Stateless Composable Contract

```kotlin
// ✅ CORRECT: Stateless, state hoisted, modifier param, max 50 lines
@Composable
fun DebtCardItem(
    debt: DebtWithScheduleUi,
    onDetailClick: () -> Unit,
    onPayClick: () -> Unit,
    modifier: Modifier = Modifier,      // Always accept modifier
) {
    // Max 50 lines of composable code
    // Use MaterialTheme.colorScheme.* tokens
    // Use MaterialTheme.typography.* tokens
}

// ❌ WRONG: Stateful, no modifier, hardcoded colors
@Composable
fun DebtCardItem(debtId: String) {
    val viewModel: DebtViewModel = hiltViewModel()
    val debt by viewModel.getDebt(debtId).collectAsState()
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF))) { ... }
}
```

### Screen Composable Contract

```kotlin
// Screen composable: connects ViewModel to stateless components
@Composable
fun DashboardScreen(
    onNavigate: (Any) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Collect one-shot events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is GlobalUiEvent.Navigate -> onNavigate(event.route)
                is GlobalUiEvent.ShowSnackbar -> { /* handled by scaffold */ }
                is GlobalUiEvent.NavigateBack -> { /* N/A for dashboard */ }
            }
        }
    }

    // Delegate to stateless content
    DashboardContent(
        uiState = uiState,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onAction: (DashboardUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is DashboardUiState.Loading -> LoadingIndicator()
        is DashboardUiState.Error -> ErrorState(message = uiState.message)
        is DashboardUiState.Success -> {
            LazyColumn(modifier = modifier) {
                item { ProfitHeroCard(uiState.todaySummary, uiState.percentChange) }
                item { IncomeExpenseRow(uiState.todaySummary) }
                item { DailyTargetSection(uiState.dailyTarget) }
                item { BudgetRemainingCard(uiState.budgetRemaining) }
                if (uiState.dueAlerts.isNotEmpty()) {
                    item { DueAlertCard(uiState.dueAlerts.first()) }
                }
                item {
                    TodayTransactionList(
                        transactions = uiState.recentTransactions,
                        onSeeAllClick = { onAction(DashboardUiAction.SeeAllTransactions) },
                    )
                }
            }
        }
    }
}
```

### Performance Rules

| Rule | Implementation |
|------|----------------|
| LazyColumn key | `items(list, key = { it.id })` — always provide stable key |
| @Stable / @Immutable | Annotate UI model data classes used in Compose |
| Avoid unnecessary recomposition | Don't pass lambda that captures mutable state. Use `remember` + method reference |
| collectAsStateWithLifecycle | Always use instead of `collectAsState()` — lifecycle-aware |
| Modifier parameter | Every composable must accept `modifier: Modifier = Modifier` as last param |

---

## 10. Coroutines & Flow Patterns

### ViewModel Pattern

```kotlin
// Expose state as StateFlow (read-only)
val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()

// Expose one-shot events as Channel → Flow
private val _uiEvent = Channel<GlobalUiEvent>(Channel.BUFFERED)
val uiEvent: Flow<GlobalUiEvent> = _uiEvent.receiveAsFlow()

// Send events
viewModelScope.launch {
    _uiEvent.send(GlobalUiEvent.ShowSnackbar("Success!"))
}
```

### Repository Pattern — Observe vs One-Shot

```kotlin
interface TransactionRepository {
    // OBSERVE: returns Flow for real-time updates
    fun observeTodayTransactions(date: LocalDate): Flow<List<Transaction>>

    // ONE-SHOT: returns value directly (used in UseCase calculations)
    suspend fun getTodaySummary(date: LocalDate): TodaySummary

    // WRITE: suspend function
    suspend fun insert(transaction: Transaction)
}
```

### DAO Pattern — Flow for Observe

```kotlin
@Dao
interface TransactionDao {
    // Flow → auto-emits on data change (Room built-in)
    @Query("SELECT * FROM transactions WHERE date(created_at) = :date AND is_deleted = 0")
    fun observeByDate(date: String): Flow<List<TransactionEntity>>

    // Suspend → one-shot query
    @Query("SELECT ... FROM transactions WHERE date(created_at) = :date AND is_deleted = 0")
    suspend fun getTodaySummary(date: String): TodaySummaryRaw

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionEntity)
}
```

---

## 11. Build Configuration

### Version Catalog (`gradle/libs.versions.toml`)

```toml
[versions]
kotlin = "2.3.0"              # Kotlin 2.3.x
compose-bom = "2026.02.00"    # Compose BOM (latest)
material3 = "1.10.0"          # M3 Expressive
navigation3 = "1.0.0"         # Navigation 3
room = "2.7.0"                # Room + KSP
hilt = "2.54.0"               # Hilt
ksp = "2.3.0-1.0.0"           # KSP matching Kotlin
datastore = "1.1.0"           # DataStore
coil = "3.1.0"                # Coil 3
kotlinx-serialization = "1.8.0"
coroutines = "1.10.0"
lifecycle = "2.9.0"

# Testing
junit5 = "5.11.0"
mockk = "1.13.16"
turbine = "1.2.0"
paparazzi = "1.3.5"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }

# Navigation 3
navigation3 = { group = "androidx.navigation3", name = "navigation3", version.ref = "navigation3" }

# Lifecycle
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# DataStore
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Serialization
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

# Coil
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }

# Coroutines
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Testing
junit5-api = { group = "org.junit.jupiter", name = "junit-jupiter-api", version.ref = "junit5" }
junit5-engine = { group = "org.junit.jupiter", name = "junit-jupiter-engine", version.ref = "junit5" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }
compose-ui-test = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }

[plugins]
android-application = { id = "com.android.application", version = "8.8.0" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
room = { id = "androidx.room", version.ref = "room" }
paparazzi = { id = "app.cash.paparazzi", version.ref = "paparazzi" }
```

### App build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
}

android {
    namespace = "com.driverwallet.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.driverwallet.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation 3
    implementation(libs.navigation3)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // DataStore
    implementation(libs.datastore.preferences)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coil
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.compose.ui.test)
    debugImplementation(libs.compose.ui.test.manifest)
}
```

---

## 12. Utility Classes

### Currency Formatter

```kotlin
// file: core/util/CurrencyFormatter.kt
object CurrencyFormatter {

    private val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
        isGroupingUsed = true
        maximumFractionDigits = 0
    }

    /** 150000 → "150.000" */
    fun format(amount: Long): String = numberFormat.format(amount)

    /** 150000 → "Rp 150.000" */
    fun formatWithPrefix(amount: Long): String = "Rp ${format(amount)}"

    /** 150000 → "150rb" (untuk space yang sempit) */
    fun formatShort(amount: Long): String = when {
        amount >= 1_000_000_000 -> "${amount / 1_000_000_000}M"
        amount >= 1_000_000 -> "${amount / 1_000_000}jt"
        amount >= 1_000 -> "${amount / 1_000}rb"
        else -> amount.toString()
    }

    /** 150000 → "Rp 150rb" */
    fun formatShortWithPrefix(amount: Long): String = "Rp ${formatShort(amount)}"
}
```

### DateTime Extensions

```kotlin
// file: core/model/DateTimeExt.kt
val JAKARTA_ZONE: ZoneId = ZoneId.of("Asia/Jakarta")

fun nowJakarta(): ZonedDateTime = ZonedDateTime.now(JAKARTA_ZONE)

fun todayJakarta(): LocalDate = LocalDate.now(JAKARTA_ZONE)

fun LocalDate.toDisplayString(): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID"))
    return this.format(formatter)
}

fun LocalDate.startOfWeek(): LocalDate =
    this.with(DayOfWeek.MONDAY)

fun LocalDate.endOfWeek(): LocalDate =
    this.with(DayOfWeek.SUNDAY)

fun LocalDate.startOfMonth(): LocalDate =
    this.withDayOfMonth(1)

fun LocalDate.endOfMonth(): LocalDate =
    this.withDayOfMonth(this.lengthOfMonth())
```

---

## 13. Accessibility Requirements

| Requirement | Implementation |
|-------------|----------------|
| Content description | Setiap `Icon()` harus punya `contentDescription` yang bermakna dalam Bahasa Indonesia |
| Touch target | Minimum 48dp × 48dp untuk semua interactive elements. Gunakan `Modifier.minimumInteractiveComponentSize()` |
| TalkBack | Test semua screen dengan TalkBack enabled |
| Color contrast | Minimum 4.5:1 ratio untuk text (mengikuti M3 color system otomatis) |
| Semantic grouping | Gunakan `Modifier.semantics(mergeDescendants = true)` untuk card yang merupakan satu unit informasi |

---

## 14. Security Checklist

| Item | Implementation |
|------|----------------|
| No hardcoded secrets | Tidak ada API key, token, atau secret di source code |
| EncryptedDataStore | Jika menyimpan data sensitif, gunakan EncryptedDataStore |
| ProGuard/R8 | Enable minification di release build |
| Export schema | Room schema di-export ke `schemas/` untuk migration testing |
| SQL injection | Room menggunakan parameterized queries secara default — aman |
| FileProvider | CSV export via FileProvider, bukan file:// URI |

---

*Dokumen ini adalah bagian 3 dari 5. Lanjut ke [AD_04_Data_Model.md](./AD_04_Data_Model.md) untuk Room entities, DAOs, dan validation rules.*
