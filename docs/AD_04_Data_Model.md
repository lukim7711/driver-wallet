# AD_04: Data Model — Driver Wallet Android

> **Audience:** AI Builder & Developer
> **Database:** Room (SQLite), 100% offline, single-user
> **Prerequisite:** Baca [AD_03_Tech_Architecture.md](./AD_03_Tech_Architecture.md) untuk database setup

---

## 1. Entity Relationship Diagram (ERD)

```
┌───────────────────┐
│   transactions    │
│   (PK: id)        │
│                   │
│  type             │
│  amount           │
│  category         │
│  note             │
│  source           │
│  debt_id (FK?) ──────────────────────────┐
│  created_at       │                          │
│  is_deleted       │                          │
└───────────────────┘                          │
                                                │
┌───────────────────┐       ┌───────────────────┐
│  debt_schedules   │       │     debts         │
│  (PK: id)         │       │     (PK: id)      │◄──┘
│                   │       │                   │
│  debt_id (FK) ─────────►│  platform        │
│  installment_no   │       │  total_amount     │
│  due_date         │       │  remaining_amount │
│  amount           │       │  installment_per  │
│  actual_amount    │       │  installment_count│
│  status           │       │  due_day          │
│  paid_at          │       │  interest_rate    │
│  is_deleted       │       │  penalty_type     │
└───────────────────┘       │  penalty_rate     │
                            │  debt_type        │
                            │  status           │
                            │  note             │
                            │  start_date       │
                            │  created_at       │
                            │  is_deleted       │
                            └───────────────────┘

┌───────────────────┐   ┌───────────────────┐   ┌───────────────────┐
│  daily_budgets    │   │ monthly_expenses  │   │  daily_expenses   │
│  (PK: id)         │   │ (PK: id)          │   │  (PK: id)         │
│                   │   │                   │   │                   │
│  category         │   │  name             │   │  name             │
│  amount           │   │  description      │   │  description      │
│  is_deleted       │   │  icon             │   │  icon             │
└───────────────────┘   │  amount           │   │  amount           │
                        │  is_deleted       │   │  is_deleted       │
                        └───────────────────┘   └───────────────────┘

┌───────────────────┐
│    settings       │
│    (PK: key)      │
│                   │
│  value            │
└───────────────────┘
```

### Relationships

| Parent | Child | Relationship | FK Column |
|--------|-------|-------------|----------|
| `debts` | `debt_schedules` | 1 → Many | `debt_schedules.debt_id` |
| `debts` | `transactions` | 1 → Many (optional) | `transactions.debt_id` (nullable) |

> **Catatan:** Room tidak enforce Foreign Key secara default. Gunakan `@ForeignKey` annotation untuk data integrity.

---

## 2. Entity Definitions

### 2.1 TransactionEntity

```kotlin
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["created_at"]),
        Index(value = ["type"]),
        Index(value = ["category"]),
        Index(value = ["debt_id"]),
        Index(value = ["is_deleted"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = DebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debt_id"],
            onDelete = ForeignKey.SET_NULL,
        )
    ]
)
data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,                          // UUID string

    @ColumnInfo(name = "created_at")
    val createdAt: String,                   // ISO 8601 with timezone: "2024-10-20T13:45:00+07:00"

    @ColumnInfo(name = "type")
    val type: String,                        // "income" | "expense"

    @ColumnInfo(name = "amount")
    val amount: Long,                        // Integer Rupiah (contoh: 15000)

    @ColumnInfo(name = "category")
    val category: String,                    // ID kategori (contoh: "order", "bbm", "cicilan")

    @ColumnInfo(name = "note", defaultValue = "")
    val note: String = "",                   // Catatan opsional, max 100 char

    @ColumnInfo(name = "source", defaultValue = "manual")
    val source: String = "manual",           // "manual" | "debt_payment"

    @ColumnInfo(name = "debt_id", defaultValue = "NULL")
    val debtId: String? = null,              // FK ke debts.id (jika source = "debt_payment")

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean = false,          // Soft delete flag
)
```

#### Column Rules

| Column | Type | Required | Validation |
|--------|------|----------|------------|
| `id` | TEXT PK | ✅ | UUID v4 format |
| `created_at` | TEXT | ✅ | ISO 8601 with timezone `+07:00` |
| `type` | TEXT | ✅ | Hanya "income" atau "expense" |
| `amount` | INTEGER | ✅ | > 0, max 999.999.999 |
| `category` | TEXT | ✅ | Harus ada di daftar kategori (lihat section 5) |
| `note` | TEXT | ❌ | Max 100 karakter, default "" |
| `source` | TEXT | ✅ | Hanya "manual" atau "debt_payment" |
| `debt_id` | TEXT | ❌ | Wajib jika source = "debt_payment" |
| `is_deleted` | INTEGER (0/1) | ✅ | Default 0 (false) |

---

### 2.2 DebtEntity

```kotlin
@Entity(
    tableName = "debts",
    indices = [
        Index(value = ["status"]),
        Index(value = ["is_deleted"]),
    ]
)
data class DebtEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,                          // UUID string

    @ColumnInfo(name = "platform")
    val platform: String,                    // Nama platform: "Shopee Pinjam", "GoPay Later", dll

    @ColumnInfo(name = "total_amount")
    val totalAmount: Long,                   // Total pinjaman awal (Rp)

    @ColumnInfo(name = "remaining_amount")
    val remainingAmount: Long,               // Sisa hutang saat ini (Rp)

    @ColumnInfo(name = "installment_per_month")
    val installmentPerMonth: Long,           // Nominal cicilan per bulan (Rp)

    @ColumnInfo(name = "installment_count")
    val installmentCount: Int,               // Jumlah total cicilan (contoh: 6)

    @ColumnInfo(name = "due_day")
    val dueDay: Int,                         // Tanggal jatuh tempo setiap bulan (1-31)

    @ColumnInfo(name = "interest_rate", defaultValue = "0.0")
    val interestRate: Double = 0.0,          // Bunga per bulan dalam persen (contoh: 2.5)

    @ColumnInfo(name = "penalty_type", defaultValue = "none")
    val penaltyType: String = "none",        // "none" | "fixed" | "percentage"

    @ColumnInfo(name = "penalty_rate", defaultValue = "0.0")
    val penaltyRate: Double = 0.0,           // Nominal/persentase denda

    @ColumnInfo(name = "debt_type", defaultValue = "lainnya")
    val debtType: String = "lainnya",        // "pinjaman_tunai" | "paylater" | "cicilan_barang" | "lainnya"

    @ColumnInfo(name = "status", defaultValue = "active")
    val status: String = "active",           // "active" | "completed"

    @ColumnInfo(name = "note", defaultValue = "")
    val note: String = "",                   // Catatan opsional, max 200 char

    @ColumnInfo(name = "start_date")
    val startDate: String,                   // ISO date: "2024-10-01"

    @ColumnInfo(name = "created_at")
    val createdAt: String,                   // ISO 8601 with timezone

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean = false,
)
```

#### Column Rules

| Column | Type | Required | Validation |
|--------|------|----------|------------|
| `id` | TEXT PK | ✅ | UUID v4 |
| `platform` | TEXT | ✅ | Non-empty, max 100 char |
| `total_amount` | INTEGER | ✅ | > 0 |
| `remaining_amount` | INTEGER | ✅ | ≥ 0, ≤ total_amount saat pembuatan |
| `installment_per_month` | INTEGER | ✅ | > 0 |
| `installment_count` | INTEGER | ✅ | > 0, max 120 (10 tahun) |
| `due_day` | INTEGER | ✅ | 1–31 |
| `interest_rate` | REAL | ❌ | ≥ 0.0, default 0.0 |
| `penalty_type` | TEXT | ❌ | "none" \| "fixed" \| "percentage" |
| `penalty_rate` | REAL | ❌ | ≥ 0.0 |
| `debt_type` | TEXT | ❌ | Enum string |
| `status` | TEXT | ✅ | "active" \| "completed" |
| `start_date` | TEXT | ✅ | ISO date format |
| `is_deleted` | INTEGER | ✅ | Default 0 |

---

### 2.3 DebtScheduleEntity

```kotlin
@Entity(
    tableName = "debt_schedules",
    indices = [
        Index(value = ["debt_id"]),
        Index(value = ["status"]),
        Index(value = ["due_date"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = DebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debt_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class DebtScheduleEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,                          // UUID string

    @ColumnInfo(name = "debt_id")
    val debtId: String,                      // FK ke debts.id

    @ColumnInfo(name = "installment_number")
    val installmentNumber: Int,              // Cicilan ke-X (1-based)

    @ColumnInfo(name = "due_date")
    val dueDate: String,                     // ISO date: "2024-11-25"

    @ColumnInfo(name = "amount")
    val amount: Long,                        // Nominal cicilan yang dijadwalkan (Rp)

    @ColumnInfo(name = "actual_amount", defaultValue = "NULL")
    val actualAmount: Long? = null,          // Nominal yang benar-benar dibayar (bisa beda)

    @ColumnInfo(name = "status", defaultValue = "unpaid")
    val status: String = "unpaid",           // "unpaid" | "paid" | "overdue"

    @ColumnInfo(name = "paid_at", defaultValue = "NULL")
    val paidAt: String? = null,              // ISO 8601, null jika belum bayar

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean = false,
)
```

#### Column Rules

| Column | Type | Required | Validation |
|--------|------|----------|------------|
| `id` | TEXT PK | ✅ | UUID v4 |
| `debt_id` | TEXT FK | ✅ | Harus ada di debts.id |
| `installment_number` | INTEGER | ✅ | 1 ≤ x ≤ debt.installment_count |
| `due_date` | TEXT | ✅ | ISO date format |
| `amount` | INTEGER | ✅ | > 0 |
| `actual_amount` | INTEGER | ❌ | Null jika belum bayar, > 0 jika sudah |
| `status` | TEXT | ✅ | "unpaid" \| "paid" \| "overdue" |
| `paid_at` | TEXT | ❌ | Null jika unpaid, ISO 8601 jika paid |

#### Schedule Generation Logic

```kotlin
// Dipanggil saat tambah hutang baru
fun generateSchedules(debt: DebtEntity): List<DebtScheduleEntity> {
    val startDate = LocalDate.parse(debt.startDate)
    return (1..debt.installmentCount).map { i ->
        val dueDate = calculateDueDate(startDate, i, debt.dueDay)
        DebtScheduleEntity(
            id = UUID.randomUUID().toString(),
            debtId = debt.id,
            installmentNumber = i,
            dueDate = dueDate.toString(),  // ISO format
            amount = debt.installmentPerMonth,
            status = "unpaid",
        )
    }
}

// Hitung tanggal jatuh tempo dengan handle bulan yang hari-nya < dueDay
private fun calculateDueDate(startDate: LocalDate, monthOffset: Int, dueDay: Int): LocalDate {
    val targetMonth = startDate.plusMonths(monthOffset.toLong())
    val maxDay = targetMonth.lengthOfMonth()
    val actualDay = minOf(dueDay, maxDay)  // Handle Feb 28/29, bulan 30 hari
    return targetMonth.withDayOfMonth(actualDay)
}
```

---

### 2.4 DailyBudgetEntity

```kotlin
@Entity(
    tableName = "daily_budgets",
    indices = [Index(value = ["category"], unique = true)]
)
data class DailyBudgetEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,                          // UUID string

    @ColumnInfo(name = "category")
    val category: String,                    // "bbm" | "makan" | "rokok" | "pulsa"

    @ColumnInfo(name = "amount")
    val amount: Long,                        // Budget harian per kategori (Rp)

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean = false,
)
```

#### Seed Data (Pre-populated)

| category | amount (default) | Label UI |
|----------|-----------------|----------|
| `bbm` | 30000 | BBM |
| `makan` | 50000 | Makan |
| `rokok` | 25000 | Rokok |
| `pulsa` | 5000 | Pulsa / Data |

---

### 2.5 MonthlyExpenseEntity

```kotlin
@Entity(
    tableName = "monthly_expenses",
    indices = [Index(value = ["is_deleted"])]
)
data class MonthlyExpenseEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,                          // UUID string

    @ColumnInfo(name = "name")
    val name: String,                        // "Rumah Tangga"

    @ColumnInfo(name = "description", defaultValue = "")
    val description: String = "",            // "Bayar Kontrakan"

    @ColumnInfo(name = "icon", defaultValue = "payments")
    val icon: String = "payments",           // Material icon name: "home", "school"

    @ColumnInfo(name = "amount")
    val amount: Long,                        // Nominal per bulan (Rp)

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean = false,
)
```

---

### 2.6 DailyExpenseEntity

```kotlin
@Entity(
    tableName = "daily_expenses",
    indices = [Index(value = ["is_deleted"])]
)
data class DailyExpenseEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,                          // UUID string

    @ColumnInfo(name = "name")
    val name: String,                        // "Parkir"

    @ColumnInfo(name = "description", defaultValue = "")
    val description: String = "",            // "Langganan Stasiun"

    @ColumnInfo(name = "icon", defaultValue = "payments")
    val icon: String = "payments",           // Material icon name: "local_parking"

    @ColumnInfo(name = "amount")
    val amount: Long,                        // Nominal per hari (Rp)

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    val isDeleted: Boolean = false,
)
```

---

### 2.7 SettingsEntity

```kotlin
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "key")
    val key: String,                         // Setting key

    @ColumnInfo(name = "value")
    val value: String,                       // Setting value (always stored as String)
)
```

#### Settings Keys & Values

| Key | Type (parsed) | Contoh Value | Deskripsi |
|-----|--------------|-------------|----------|
| `debt_target_date` | LocalDate | `"2024-12-31"` | Target tanggal lunas semua hutang |
| `rest_days` | List\<Int\> | `"0,6"` | Hari libur: 0=Minggu, 6=Sabtu (comma-separated) |

---

## 3. DAO Definitions

### 3.1 TransactionDao

```kotlin
@Dao
interface TransactionDao {

    // ==================== INSERT ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<TransactionEntity>)

    // ==================== QUERY: Today Summary ====================

    /**
     * Ringkasan hari ini: total income, expense, debt payment, jumlah transaksi.
     * Digunakan di: Dashboard Hero Card, Income/Expense Cards
     */
    @Query("""
        SELECT
            COALESCE(SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END), 0) AS totalIncome,
            COALESCE(SUM(CASE WHEN type = 'expense' AND source != 'debt_payment' THEN amount ELSE 0 END), 0) AS totalExpense,
            COALESCE(SUM(CASE WHEN source = 'debt_payment' THEN amount ELSE 0 END), 0) AS totalDebtPayment,
            COUNT(*) AS transactionCount
        FROM transactions
        WHERE date(created_at) = :date
          AND is_deleted = 0
    """)
    suspend fun getTodaySummary(date: String): TodaySummaryRaw

    /**
     * Data class untuk hasil query summary.
     */
    data class TodaySummaryRaw(
        val totalIncome: Long,
        val totalExpense: Long,
        val totalDebtPayment: Long,
        val transactionCount: Int,
    )

    // ==================== QUERY: Recent Transactions ====================

    /**
     * Transaksi terbaru hari ini, sorted by created_at DESC.
     * Digunakan di: Dashboard Transaction List
     */
    @Query("""
        SELECT * FROM transactions
        WHERE date(created_at) = :date
          AND is_deleted = 0
        ORDER BY created_at DESC
        LIMIT :limit
    """)
    suspend fun getRecentByDate(date: String, limit: Int = 5): List<TransactionEntity>

    /**
     * Semua transaksi hari ini (untuk "Lihat Semua").
     */
    @Query("""
        SELECT * FROM transactions
        WHERE date(created_at) = :date
          AND is_deleted = 0
        ORDER BY created_at DESC
    """)
    fun observeByDate(date: String): Flow<List<TransactionEntity>>

    // ==================== QUERY: Reports ====================

    /**
     * Summary per hari dalam range (untuk bar chart mingguan).
     * Digunakan di: Laporan Mingguan
     */
    @Query("""
        SELECT
            date(created_at) AS date,
            COALESCE(SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END), 0) AS totalIncome,
            COALESCE(SUM(CASE WHEN type = 'expense' THEN amount ELSE 0 END), 0) AS totalExpense,
            COUNT(*) AS transactionCount
        FROM transactions
        WHERE date(created_at) BETWEEN :startDate AND :endDate
          AND is_deleted = 0
        GROUP BY date(created_at)
        ORDER BY date(created_at) ASC
    """)
    suspend fun getDailySummary(startDate: String, endDate: String): List<DailySummaryRaw>

    data class DailySummaryRaw(
        val date: String,
        val totalIncome: Long,
        val totalExpense: Long,
        val transactionCount: Int,
    )

    /**
     * Summary per kategori dalam range (untuk breakdown bulanan).
     * Digunakan di: Laporan Bulanan
     */
    @Query("""
        SELECT
            category,
            type,
            COALESCE(SUM(amount), 0) AS totalAmount,
            COUNT(*) AS count
        FROM transactions
        WHERE date(created_at) BETWEEN :startDate AND :endDate
          AND is_deleted = 0
        GROUP BY category, type
        ORDER BY totalAmount DESC
    """)
    suspend fun getCategorySummary(startDate: String, endDate: String): List<CategorySummaryRaw>

    data class CategorySummaryRaw(
        val category: String,
        val type: String,
        val totalAmount: Long,
        val count: Int,
    )

    /**
     * Semua transaksi dalam range (untuk CSV export).
     * Digunakan di: Export CSV
     */
    @Query("""
        SELECT * FROM transactions
        WHERE date(created_at) BETWEEN :startDate AND :endDate
          AND is_deleted = 0
        ORDER BY created_at ASC
    """)
    suspend fun getByDateRange(startDate: String, endDate: String): List<TransactionEntity>

    // ==================== QUERY: Budget Tracking ====================

    /**
     * Total pengeluaran per kategori budget hari ini.
     * Digunakan di: Dashboard Budget Remaining
     */
    @Query("""
        SELECT
            category,
            COALESCE(SUM(amount), 0) AS totalSpent
        FROM transactions
        WHERE date(created_at) = :date
          AND type = 'expense'
          AND category IN ('bbm', 'makan', 'rokok', 'pulsa')
          AND is_deleted = 0
        GROUP BY category
    """)
    suspend fun getBudgetSpentToday(date: String): List<BudgetSpentRaw>

    data class BudgetSpentRaw(
        val category: String,
        val totalSpent: Long,
    )

    // ==================== UPDATE ====================

    @Query("UPDATE transactions SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)
}
```

---

### 3.2 DebtDao

```kotlin
@Dao
interface DebtDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DebtEntity)

    @Update
    suspend fun update(entity: DebtEntity)

    @Upsert
    suspend fun upsert(entity: DebtEntity)

    /**
     * Get single debt by ID.
     */
    @Query("SELECT * FROM debts WHERE id = :id AND is_deleted = 0")
    suspend fun getById(id: String): DebtEntity?

    /**
     * Semua hutang aktif (belum lunas, belum dihapus).
     * Digunakan di: DebtListScreen
     */
    @Query("""
        SELECT * FROM debts
        WHERE status = 'active'
          AND is_deleted = 0
        ORDER BY created_at DESC
    """)
    fun observeActiveDebts(): Flow<List<DebtEntity>>

    /**
     * Hutang yang sudah lunas (untuk riwayat).
     */
    @Query("""
        SELECT * FROM debts
        WHERE status = 'completed'
          AND is_deleted = 0
        ORDER BY created_at DESC
    """)
    fun observeCompletedDebts(): Flow<List<DebtEntity>>

    /**
     * Total sisa hutang aktif.
     * Digunakan di: Debt Hero Card, Dashboard DailyTarget
     */
    @Query("""
        SELECT COALESCE(SUM(remaining_amount), 0)
        FROM debts
        WHERE status = 'active'
          AND is_deleted = 0
    """)
    suspend fun getTotalRemaining(): Long

    /**
     * Update remaining amount setelah bayar cicilan.
     */
    @Query("UPDATE debts SET remaining_amount = :amount WHERE id = :id")
    suspend fun updateRemaining(id: String, amount: Long)

    /**
     * Update status hutang (active -> completed).
     */
    @Query("UPDATE debts SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE debts SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)
}
```

---

### 3.3 DebtScheduleDao

```kotlin
@Dao
interface DebtScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DebtScheduleEntity>)

    /**
     * Semua jadwal cicilan untuk satu hutang.
     * Digunakan di: Debt Detail
     */
    @Query("""
        SELECT * FROM debt_schedules
        WHERE debt_id = :debtId
          AND is_deleted = 0
        ORDER BY installment_number ASC
    """)
    suspend fun getByDebtId(debtId: String): List<DebtScheduleEntity>

    /**
     * Jadwal cicilan UNPAID berikutnya untuk satu hutang.
     * Digunakan di: PaymentBottomSheet
     */
    @Query("""
        SELECT * FROM debt_schedules
        WHERE debt_id = :debtId
          AND status = 'unpaid'
          AND is_deleted = 0
        ORDER BY installment_number ASC
        LIMIT 1
    """)
    suspend fun getNextUnpaid(debtId: String): DebtScheduleEntity?

    /**
     * Jumlah cicilan yang sudah dibayar untuk satu hutang.
     * Digunakan di: Progress bar debt card
     */
    @Query("""
        SELECT COUNT(*) FROM debt_schedules
        WHERE debt_id = :debtId
          AND status = 'paid'
          AND is_deleted = 0
    """)
    suspend fun countPaid(debtId: String): Int

    /**
     * Jumlah cicilan yang belum dibayar.
     * Digunakan di: Lunas detection
     */
    @Query("""
        SELECT COUNT(*) FROM debt_schedules
        WHERE debt_id = :debtId
          AND status != 'paid'
          AND is_deleted = 0
    """)
    suspend fun countUnpaid(debtId: String): Int

    /**
     * Jadwal yang mendekati jatuh tempo (untuk alert di Dashboard).
     * Digunakan di: Dashboard DueAlertCard
     */
    @Query("""
        SELECT ds.*, d.platform, d.id AS parent_debt_id
        FROM debt_schedules ds
        INNER JOIN debts d ON ds.debt_id = d.id
        WHERE ds.status = 'unpaid'
          AND ds.is_deleted = 0
          AND d.is_deleted = 0
          AND d.status = 'active'
          AND ds.due_date <= :maxDate
        ORDER BY ds.due_date ASC
    """)
    suspend fun getUpcomingDue(maxDate: String): List<UpcomingDueRaw>

    data class UpcomingDueRaw(
        val id: String,
        @ColumnInfo(name = "debt_id") val debtId: String,
        @ColumnInfo(name = "installment_number") val installmentNumber: Int,
        @ColumnInfo(name = "due_date") val dueDate: String,
        val amount: Long,
        val status: String,
        val platform: String,
        @ColumnInfo(name = "parent_debt_id") val parentDebtId: String,
    )

    /**
     * Mark cicilan sebagai paid.
     */
    @Query("""
        UPDATE debt_schedules
        SET status = 'paid',
            paid_at = :paidAt,
            actual_amount = :actualAmount
        WHERE id = :id
    """)
    suspend fun markAsPaid(id: String, paidAt: String, actualAmount: Long)

    /**
     * Update status overdue untuk jadwal yang lewat jatuh tempo.
     * Dipanggil saat app dibuka (DashboardViewModel.init).
     */
    @Query("""
        UPDATE debt_schedules
        SET status = 'overdue'
        WHERE status = 'unpaid'
          AND due_date < :today
          AND is_deleted = 0
    """)
    suspend fun markOverdueSchedules(today: String)
}
```

---

### 3.4 DailyBudgetDao

```kotlin
@Dao
interface DailyBudgetDao {

    @Query("SELECT * FROM daily_budgets WHERE is_deleted = 0 ORDER BY category ASC")
    suspend fun getAll(): List<DailyBudgetEntity>

    @Query("SELECT * FROM daily_budgets WHERE is_deleted = 0 ORDER BY category ASC")
    fun observeAll(): Flow<List<DailyBudgetEntity>>

    @Upsert
    suspend fun upsertAll(entities: List<DailyBudgetEntity>)

    /**
     * Total budget harian.
     * Digunakan di: Dashboard BudgetRemaining
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM daily_budgets WHERE is_deleted = 0")
    suspend fun getTotalBudget(): Long
}
```

---

### 3.5 MonthlyExpenseDao

```kotlin
@Dao
interface MonthlyExpenseDao {

    @Query("SELECT * FROM monthly_expenses WHERE is_deleted = 0 ORDER BY name ASC")
    suspend fun getAll(): List<MonthlyExpenseEntity>

    @Query("SELECT * FROM monthly_expenses WHERE is_deleted = 0 ORDER BY name ASC")
    fun observeAll(): Flow<List<MonthlyExpenseEntity>>

    @Upsert
    suspend fun upsert(entity: MonthlyExpenseEntity)

    @Query("UPDATE monthly_expenses SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)

    /**
     * Total pengeluaran tetap bulanan.
     * Digunakan di: DailyTarget calculation (prorated)
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM monthly_expenses WHERE is_deleted = 0")
    suspend fun getTotalAmount(): Long
}
```

---

### 3.6 DailyExpenseDao

```kotlin
@Dao
interface DailyExpenseDao {

    @Query("SELECT * FROM daily_expenses WHERE is_deleted = 0 ORDER BY name ASC")
    suspend fun getAll(): List<DailyExpenseEntity>

    @Query("SELECT * FROM daily_expenses WHERE is_deleted = 0 ORDER BY name ASC")
    fun observeAll(): Flow<List<DailyExpenseEntity>>

    @Upsert
    suspend fun upsert(entity: DailyExpenseEntity)

    @Query("UPDATE daily_expenses SET is_deleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)

    /**
     * Total pengeluaran tetap harian.
     * Digunakan di: DailyTarget calculation
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM daily_expenses WHERE is_deleted = 0")
    suspend fun getTotalAmount(): Long
}
```

---

### 3.7 SettingsDao

```kotlin
@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE `key` = :key")
    suspend fun getByKey(key: String): SettingsEntity?

    @Query("SELECT value FROM settings WHERE `key` = :key")
    suspend fun getValue(key: String): String?

    @Query("SELECT value FROM settings WHERE `key` = :key")
    fun observeValue(key: String): Flow<String?>

    @Upsert
    suspend fun upsert(entity: SettingsEntity)

    @Query("DELETE FROM settings WHERE `key` = :key")
    suspend fun delete(key: String)
}
```

---

## 4. Compound Query: DebtWithSchedule

Untuk menampilkan debt card dengan info cicilan, gunakan data class gabungan:

```kotlin
/**
 * Data class non-entity untuk hasil gabungan Debt + progress cicilan.
 * Digunakan di: DebtListScreen, Dashboard DueAlert
 */
data class DebtWithScheduleInfo(
    val debt: DebtEntity,
    val nextSchedule: DebtScheduleEntity?,  // Cicilan unpaid berikutnya
    val paidCount: Int,                     // Jumlah cicilan sudah bayar
    val totalScheduleCount: Int,            // Total jadwal cicilan
    val paidPercentage: Float,              // paidCount / totalScheduleCount * 100
)
```

### Repository Implementation

```kotlin
class DebtRepositoryImpl @Inject constructor(
    private val debtDao: DebtDao,
    private val scheduleDao: DebtScheduleDao,
    private val transactionDao: TransactionDao,
    private val database: AppDatabase,
) : DebtRepository {

    override fun observeActiveDebtsWithSchedule(): Flow<List<DebtWithScheduleInfo>> {
        return debtDao.observeActiveDebts().map { debts ->
            debts.map { debt ->
                val nextSchedule = scheduleDao.getNextUnpaid(debt.id)
                val paidCount = scheduleDao.countPaid(debt.id)
                val totalCount = debt.installmentCount
                DebtWithScheduleInfo(
                    debt = debt,
                    nextSchedule = nextSchedule,
                    paidCount = paidCount,
                    totalScheduleCount = totalCount,
                    paidPercentage = if (totalCount > 0) {
                        (paidCount.toFloat() / totalCount * 100)
                    } else 0f,
                )
            }
        }
    }

    /**
     * Atomic payment: Room @Transaction memastikan semua operasi
     * berhasil atau semua di-rollback.
     */
    override suspend fun payInstallment(
        debtId: String,
        scheduleId: String,
        amount: Long,
    ) {
        database.withTransaction {
            // 1. Update debt remaining
            val debt = debtDao.getById(debtId)
                ?: throw IllegalStateException("Debt not found: $debtId")
            val newRemaining = (debt.remainingAmount - amount).coerceAtLeast(0)
            debtDao.updateRemaining(debtId, newRemaining)

            // 2. Mark schedule as paid
            val now = nowJakarta().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
            scheduleDao.markAsPaid(
                id = scheduleId,
                paidAt = now,
                actualAmount = amount,
            )

            // 3. Insert expense transaction
            transactionDao.insert(
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    createdAt = now,
                    type = "expense",
                    amount = amount,
                    category = "cicilan",
                    note = "Cicilan ${debt.platform}",
                    source = "debt_payment",
                    debtId = debtId,
                )
            )

            // 4. Check if fully paid
            val unpaidCount = scheduleDao.countUnpaid(debtId)
            if (unpaidCount == 0 || newRemaining <= 0) {
                debtDao.updateStatus(debtId, "completed")
            }
        }
    }
}
```

---

## 5. Category Constants

```kotlin
/**
 * Definisi kategori yang dipakai di seluruh app.
 * AI Builder: gunakan data class ini sebagai single source of truth.
 */
@Immutable
data class Category(
    val id: String,
    val label: String,           // Label Bahasa Indonesia
    val icon: String,            // Material icon name
    val type: TransactionType,   // INCOME | EXPENSE
)

enum class TransactionType(val value: String) {
    INCOME("income"),
    EXPENSE("expense"),
}

object Categories {

    val incomeCategories = listOf(
        Category("order", "Order", "shopping_bag", TransactionType.INCOME),
        Category("tips", "Tips", "savings", TransactionType.INCOME),
        Category("bonus", "Bonus", "redeem", TransactionType.INCOME),
        Category("insentif", "Insentif", "emoji_events", TransactionType.INCOME),
        Category("lainnya_in", "Lainnya", "more_horiz", TransactionType.INCOME),
    )

    val expenseCategories = listOf(
        Category("bbm", "BBM", "local_gas_station", TransactionType.EXPENSE),
        Category("makan", "Makan", "restaurant", TransactionType.EXPENSE),
        Category("rokok", "Rokok", "smoking_rooms", TransactionType.EXPENSE),
        Category("pulsa", "Pulsa", "phone_android", TransactionType.EXPENSE),
        Category("rt", "RT", "home", TransactionType.EXPENSE),
        Category("parkir", "Parkir", "local_parking", TransactionType.EXPENSE),
        Category("service", "Service", "build", TransactionType.EXPENSE),
        Category("lainnya_out", "Lainnya", "more_horiz", TransactionType.EXPENSE),
    )

    // Kategori khusus cicilan (auto, tidak tampil di grid user)
    val debtPaymentCategory = Category("cicilan", "Cicilan", "credit_card", TransactionType.EXPENSE)

    fun getByType(type: TransactionType): List<Category> = when (type) {
        TransactionType.INCOME -> incomeCategories
        TransactionType.EXPENSE -> expenseCategories
    }

    fun findById(id: String): Category? {
        return (incomeCategories + expenseCategories + debtPaymentCategory)
            .find { it.id == id }
    }
}
```

---

## 6. Domain Models (Mapped from Entity)

Domain models adalah data class yang digunakan di UseCase dan ViewModel. Terpisah dari Room Entity untuk menjaga Clean Architecture boundary.

```kotlin
// === Transaction Domain Model ===
@Immutable
data class Transaction(
    val id: String,
    val createdAt: ZonedDateTime,
    val type: TransactionType,
    val amount: Long,
    val category: Category,
    val note: String,
    val source: String,
    val debtId: String?,
)

// === Dashboard Models ===
@Immutable
data class TodaySummary(
    val totalIncome: Long,
    val totalExpense: Long,
    val totalDebtPayment: Long,
    val transactionCount: Int,
) {
    val profit: Long get() = totalIncome - totalExpense - totalDebtPayment
}

@Immutable
data class DailyTarget(
    val earnedToday: Long,        // income hari ini
    val targetAmount: Long,       // target yang harus dicapai
    val isOnTrack: Boolean,
    val percentage: Int,          // 0-100
)

@Immutable
data class BudgetInfo(
    val totalBudget: Long,
    val totalSpent: Long,
) {
    val remaining: Long get() = (totalBudget - totalSpent).coerceAtLeast(0)
    val percentage: Float get() = if (totalBudget > 0) {
        (totalSpent.toFloat() / totalBudget).coerceIn(0f, 1f)
    } else 0f
}

@Immutable
data class DueAlert(
    val debtId: String,
    val platform: String,
    val scheduleId: String,
    val dueDate: LocalDate,
    val amount: Long,
    val daysUntilDue: Int,
    val urgency: UrgencyLevel,
)

enum class UrgencyLevel {
    OVERDUE,    // days <= 0
    CRITICAL,   // 1-3 days
    WARNING,    // 4-7 days
    NORMAL,     // > 7 days
    ;

    companion object {
        fun fromDaysUntilDue(days: Int): UrgencyLevel = when {
            days <= 0 -> OVERDUE
            days <= 3 -> CRITICAL
            days <= 7 -> WARNING
            else -> NORMAL
        }
    }
}

// === Report Models ===
@Immutable
data class DailySummary(
    val date: LocalDate,
    val dayOfWeek: String,       // "Senin", "Selasa", dll
    val totalIncome: Long,
    val totalExpense: Long,
    val transactionCount: Int,
) {
    val profit: Long get() = totalIncome - totalExpense
}

@Immutable
data class CategorySummary(
    val category: Category,
    val totalAmount: Long,
    val count: Int,
    val percentage: Float,        // % dari total expense/income
)

@Immutable
data class WeeklyReport(
    val weekLabel: String,        // "Minggu Ini" | "Minggu Lalu" | "2 Minggu Lalu"
    val dateRange: Pair<LocalDate, LocalDate>,
    val dailySummaries: List<DailySummary>,  // selalu 7 items (Mon-Sun)
    val totalIncome: Long,
    val totalExpense: Long,
    val profit: Long,
)

@Immutable
data class MonthlyReport(
    val monthLabel: String,       // "Oktober 2024"
    val totalIncome: Long,
    val totalExpense: Long,
    val profit: Long,
    val incomeByCategory: List<CategorySummary>,
    val expenseByCategory: List<CategorySummary>,
)
```

---

## 7. Entity ↔ Domain Mapper

```kotlin
// file: shared/data/mapper/TransactionMapper.kt

object TransactionMapper {

    fun TransactionEntity.toDomain(): Transaction = Transaction(
        id = id,
        createdAt = ZonedDateTime.parse(createdAt),
        type = TransactionType.entries.first { it.value == type },
        amount = amount,
        category = Categories.findById(category)
            ?: Categories.expenseCategories.last(), // fallback to "Lainnya"
        note = note,
        source = source,
        debtId = debtId,
    )

    fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
        id = id,
        createdAt = createdAt.format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
        type = type.value,
        amount = amount,
        category = category.id,
        note = note,
        source = source,
        debtId = debtId,
    )
}
```

---

## 8. Database Pre-Population (Seed Data)

Saat pertama kali install, database harus di-seed dengan default data:

```kotlin
// Dipanggil via RoomDatabase.Callback
class DatabaseCallback @Inject constructor(
    private val scope: CoroutineScope,
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        scope.launch {
            seedDailyBudgets(db)
            seedSettings(db)
        }
    }

    private fun seedDailyBudgets(db: SupportSQLiteDatabase) {
        val defaults = listOf(
            Triple("bbm", 30_000L, UUID.randomUUID().toString()),
            Triple("makan", 50_000L, UUID.randomUUID().toString()),
            Triple("rokok", 25_000L, UUID.randomUUID().toString()),
            Triple("pulsa", 5_000L, UUID.randomUUID().toString()),
        )
        defaults.forEach { (category, amount, id) ->
            db.execSQL(
                "INSERT INTO daily_budgets (id, category, amount, is_deleted) VALUES (?, ?, ?, 0)",
                arrayOf(id, category, amount)
            )
        }
    }

    private fun seedSettings(db: SupportSQLiteDatabase) {
        // Default: target lunas 3 bulan dari sekarang
        val defaultTarget = LocalDate.now(JAKARTA_ZONE)
            .plusMonths(3)
            .toString()
        db.execSQL(
            "INSERT INTO settings (`key`, value) VALUES (?, ?)",
            arrayOf("debt_target_date", defaultTarget)
        )
        db.execSQL(
            "INSERT INTO settings (`key`, value) VALUES (?, ?)",
            arrayOf("rest_days", "0")  // Default: Minggu libur
        )
    }
}
```

### Database Provider with Callback

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        callback: DatabaseCallback,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "driver_wallet.db"
    )
    .addCallback(callback)
    .build()

    // ... DAO providers
}
```

---

## 9. Validation Rules Summary

### Input Validation (di UseCase / ViewModel)

| Field | Rule | Error Message (Bahasa Indonesia) |
|-------|------|----------------------------------|
| Transaction.amount | > 0, ≤ 999.999.999 | "Nominal harus lebih dari 0" / "Nominal terlalu besar" |
| Transaction.category | Harus ada di Categories | "Pilih kategori terlebih dahulu" |
| Transaction.note | Max 100 char | "Catatan maksimal 100 karakter" |
| Debt.platform | Non-empty, max 100 | "Nama platform harus diisi" |
| Debt.totalAmount | > 0 | "Total pinjaman harus lebih dari 0" |
| Debt.installmentPerMonth | > 0 | "Nominal cicilan harus lebih dari 0" |
| Debt.installmentCount | 1–120 | "Jumlah cicilan harus 1–120" |
| Debt.dueDay | 1–31 | "Tanggal jatuh tempo harus 1–31" |
| Debt.interestRate | ≥ 0.0 | "Bunga tidak boleh negatif" |
| DailyBudget.amount | ≥ 0 | "Budget tidak boleh negatif" |
| MonthlyExpense.name | Non-empty, max 100 | "Nama harus diisi" |
| MonthlyExpense.amount | > 0 | "Nominal harus lebih dari 0" |
| Settings.debt_target_date | > today | "Tanggal target harus di masa depan" |

### Data Integrity Rules (di Room @Transaction)

| Rule | Enforcement |
|------|-------------|
| Soft delete only | Semua delete = set `is_deleted = 1`, bukan physical delete |
| Debt payment atomic | Bayar cicilan = 1 Room @Transaction (update debt + schedule + insert transaction) |
| Schedule auto-gen | Saat tambah debt, auto-generate semua DebtSchedule entries |
| Overdue auto-update | Saat app dibuka, mark semua schedule yang lewat jatuh tempo sebagai "overdue" |
| Unique budget category | `daily_budgets.category` memiliki unique index |

---

## 10. Migration Strategy

```kotlin
// Untuk versi awal (v1), tidak perlu migration.
// Untuk versi berikutnya, gunakan pattern ini:

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Contoh: tambah kolom baru
        db.execSQL("ALTER TABLE transactions ADD COLUMN updated_at TEXT DEFAULT NULL")
    }
}

// Register di database builder:
Room.databaseBuilder(...)
    .addMigrations(MIGRATION_1_2)
    .build()

// PENTING: Selalu export schema untuk migration testing:
// room { schemaDirectory("$projectDir/schemas") }
```

---

*Dokumen ini adalah bagian 4 dari 5. Lanjut ke [AD_05_Implementation.md](./AD_05_Implementation.md) untuk panduan implementasi, coding standards, dan testing strategy.*
